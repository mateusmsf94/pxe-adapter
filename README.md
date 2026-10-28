# pxe-adapter

Adapter do **Pix Estático** no Split Payment da Reforma Tributária (Modelo Inteligente).

Recebe as duas mensagens de um Pix Estático, o **preliminar** (a iniciação) e a **segregação** (a liquidação), e traduz cada uma num fato do domínio que vai para o oracle-service.

Quarkus 3.40, Java 25.

## O que ele faz e o que não faz

Faz:

- Recebe só Pix Estático, nas formas `APES`, `DICT`, `MANU` e `QRES`. O roteamento por forma de iniciação acontece antes deste adapter.
- Traduz cada mensagem sozinha, sem consultar nada guardado.
- Entrega o fato ao oracle-service, com retentativa.
- Trata campo fiscal ilegível como dado, não como erro da mensagem: ele vira `CampoFiscal.Invalido` dentro do fato.

Não faz:

- Juntar preliminar e segregação. Quem junta, pelo e2eId, é o oracle-service, dono das tabelas.
- Decidir se tem split, calcular segregado, checar PJ/PF ou conferir CBS + IBS ≤ valor pago. Isso é do split-service.
- Publicar no Kafka, que é papel do oracle-service, via outbox.
- Guardar estado. Não há banco.

## Fluxo

```
preliminar (JSON) ──▶ OP do preliminar ──┐
                                         ├──▶ Tradutor ──▶ Traducao
segregação (JSON) ──▶ OP da segregação ──┘                   │
                                                             ├─ Aceito(PixEstaticoIniciado)   ──▶ oracle-service
                                                             ├─ Aceito(LiquidacaoPixEstatico) ──▶ oracle-service
                                                             ├─ Ignorado(status não final)    ──▶ contador Micrometer
                                                             └─ Malformado(referência, erros) ──▶ log (fila de erro: TODO)
```

O `Tradutor` é uma função pura: com o mesmo relógio e o mesmo cadastro, a mesma mensagem dá sempre a mesma `Traducao`.

## Modelo

```
MensagemPixEstatico = Preliminar(PayloadPreliminar) | Segregacao(PayloadSegregacao)
Traducao            = Aceito(FatoPixEstatico) | Ignorado(MotivoIgnorado) | Malformado(referencia, erros)
FatoPixEstatico     = PixEstaticoIniciado | LiquidacaoPixEstatico
ResultadoLiquidacao = Liquidado(liquidadoEm) | Rejeitado(status)
CampoFiscal<T>      = Ausente | Presente(valor) | Invalido(bruto, motivo)
```

Todos são `sealed` com records, e cada `switch` sobre eles é exaustivo, sem `default`. Uma variante nova não compila até alguém decidir o que fazer com ela.

## As operações

As duas recebem e devolvem JSON. Os caminhos são provisórios: no barramento as operações não têm nome semântico, e sim um número de OP.

### Preliminar: `POST /op/TODO-preliminar`

```json
{
  "e2eId": "E22222222202610271015abcdEFGH123",
  "formaIniciacao": "DICT",
  "valor": "500.00",
  "dataHoraPagamento": "2026-10-27T10:15:03-03:00",
  "documentoPagador": "11222333000181",
  "ispbPspPagador": "22222222",
  "documentoRecebedor": "12ABC34501DE35",
  "ispbPspRecebedor": "44444444",
  "cnpjItp": "99888777000155",
  "cbs": "46.25",
  "ibs": "88.50",
  "documentoFiscal": "NFSE-0001"
}
```

- `cnpjItp` só vem quando o Pix foi iniciado por um ITP.
- Se `ispbPspRecebedor` é o nosso ISPB, não há PSP recebedor indireto. Se não é, o PSP dele é o indireto.
- Os campos fiscais (`cbs`, `ibs` e `documentoFiscal`) podem faltar ou vir errados, e a mensagem continua aceita.
- Os demais campos são obrigatórios. Se algum falhar, a resposta lista todos os erros de uma vez.

### Segregação: `POST /op/TODO-segregacao`

```json
{
  "e2eId": "E22222222202610271015abcdEFGH123",
  "status": "ACSC",
  "dataHoraLiquidacao": "2026-10-27T10:15:04-03:00"
}
```

| `status` | Resultado |
|---|---|
| `ACSC` | `Liquidado(liquidadoEm)`; `dataHoraLiquidacao` é obrigatória |
| `RJCT` | `Rejeitado(status)` |
| qualquer outro | `Ignorado`: ainda não é final |

### Respostas

| HTTP | Quando | Corpo |
|---|---|---|
| 202 | aceita | `{"resultado":"ACEITO","referencia":"E2222…"}` |
| 202 | ignorada | `{"resultado":"IGNORADO","motivo":"status não final: PDNG"}` |
| 422 | malformada: os campos não servem, e mandar de novo não muda nada | `{"resultado":"MALFORMADO","referencia":"E2","erros":["formaIniciacao: QRDN chegou ao pxe-adapter, mas não é Pix Estático", "valor: ausente"]}` |
| 400 | o corpo nem é JSON | `{"resultado":"MALFORMADO","erros":["payload: …"]}` |
| 502 | o oracle-service recusou o fato (4xx) | `{"resultado":"NAO_REGISTRADO", …}` |
| 503 | o oracle-service não respondeu, mesmo com as retentativas; dá para tentar depois | `{"resultado":"NAO_REGISTRADO", …}` |

## O que vai para o oracle-service

Há uma operação por fato. O oracle-service é idempotente por e2eId e tipo de fato, por isso repetir é seguro. O adapter repete em falha de rede e em 5xx, até 3 vezes, com backoff exponencial a partir de 200 ms. Não repete em 4xx.

`POST /pix-estatico/iniciados`

```json
{
  "e2eId": "E22222222202610271015abcdEFGH123",
  "formaIniciacao": "DICT",
  "valorPagoCentavos": 50000,
  "pagoEm": "2026-10-27T13:15:03Z",
  "documentoPagador": "11222333000181",
  "documentoRecebedor": "12ABC34501DE35",
  "cnpjItp": "99888777000155",
  "pspPagador": "33333333",
  "pspRecebedorDireto": "AB111111",
  "pspRecebedorIndireto": "55555555",
  "cbs": {"estado": "PRESENTE", "valor": 4625},
  "ibs": {"estado": "INVALIDO", "bruto": "88,50", "motivo": "decimal fora do formato: 88,50"},
  "documentoFiscal": {"estado": "AUSENTE"},
  "chegouEm": "2026-10-28T00:56:37.378Z"
}
```

`POST /pix-estatico/liquidacoes`

```json
{"e2eId": "E2222…", "resultado": "LIQUIDADO", "liquidadoEm": "2026-10-27T13:15:04Z", "chegouEm": "…"}
{"e2eId": "E2222…", "resultado": "REJEITADO", "status": "RJCT", "chegouEm": "…"}
```

- O que é opcional e não veio é omitido.
- Valores vão em centavos.
- Instantes vão em ISO-8601, em UTC.
- Os PSPs são identificados pela raiz do CNPJ. O ISPB vira raiz pelo cadastro de participantes.

## Pacotes

```
split.pxe
├── domain/          fatos e tipos de valor; Java puro
│   └── tipos/       Centavos, Cnpj, Cpf, CnpjOuCpf, CnpjRaiz, Ispb, E2eId, DocumentoFiscal, CampoFiscal, CamposFiscais
├── service/         Tradutor, Traducao, o caso de uso e as portas; sem anotação de framework
├── resource/        as duas operações REST e o JSON de entrada
├── client/          o REST client do oracle-service, com retentativa, e o JSON de saída
├── config/          @ConfigMapping, producers CDI e o cadastro de participantes por configuração
└── monitoramento/   contador Micrometer e log das mensagens não aceitas
```

`domain` não importa nada de framework. `service` só importa `domain` e o JDK, e é ligado ao CDI pelos producers de `config`.

As portas do `service` são três:

| Porta | Para quê | Implementação |
|---|---|---|
| `RegistroPixEstatico` | entrega cada fato ao oracle-service | `client/RegistroPixEstaticoOracle` |
| `CadastroParticipantes` | ISPB → raiz do CNPJ | `config/CadastroParticipantesPorConfiguracao` |
| `MensagensNaoAceitas` | o que não vira fato: ignoradas e malformadas | `monitoramento/MensagensNaoAceitasMicrometer` |

As ignoradas viram o contador `pxe_mensagens_ignoradas_total{motivo="status_nao_final"}`, publicado em `/q/metrics`. As malformadas viram um log de warning com a referência e os erros, inclusive no MDC.

## Configuração

| Chave | O que é |
|---|---|
| `pxe.ispb-proprio` | ISPB do próprio PSP, o PSP Recebedor Direto |
| `pxe.raiz-propria` | raiz do CNPJ do próprio PSP |
| `pxe.participantes."<ISPB>"` | raiz do CNPJ de cada participante (provisório) |
| `quarkus.rest-client.oracle-service.url` | URL do oracle-service; também lida de `ORACLE_SERVICE_URL`; padrão `http://localhost:8081` |

O perfil `%dev` traz valores de exemplo. Fora dele, `pxe.*` não tem valor padrão de propósito: se faltar, a aplicação não sobe. Um ISPB ou uma raiz fora do formato também barram a subida.

## Rodar

Precisa de JDK 25 ou mais novo; o `mvnw` baixa o Maven.

```shell
./mvnw test          # testes unitários do domain e do service, sem subir o Quarkus
./mvnw quarkus:dev   # modo dev, com os valores de exemplo do perfil %dev
./mvnw package       # gera target/quarkus-app/quarkus-run.jar
```

```shell
java -Dpxe.ispb-proprio=11111111 \
     -Dpxe.raiz-propria=AB111111 \
     '-Dpxe.participantes."22222222"=33333333' \
     -Dquarkus.rest-client.oracle-service.url=http://oracle-service:8080 \
     -jar target/quarkus-app/quarkus-run.jar
```

## Em aberto

Cada item está marcado como `TODO` no código.

1. **Status finais da segregação.** Hoje: `ACSC` = liquidado, `RJCT` = rejeitado, o resto não é final. Falta confirmar no catálogo do SPI usado pelo banco.
2. **Números das OPs.** Os caminhos das duas operações são provisórios.
3. **Campos dos payloads de entrada.** Os nomes atuais são os do domínio, provisórios. `docFiscal`, o nome do manual da PP, pode voltar.
4. **Formato da resposta e códigos HTTP**, a combinar com quem chama as operações.
5. **Contrato com o oracle-service**: caminhos e corpo dos dois POSTs.
6. **Cadastro real de participantes.** Hoje vem da configuração.
7. **Fila de erro e alerta para as malformadas.** Hoje é só log.
8. **Forma `INIC`.** Se um Pix iniciado por ITP chegar com forma `INIC`, hoje vira malformado.
9. **Autenticação das operações.** Hoje as duas operações estão abertas, e quem alcançar o serviço consegue registrar fatos no oracle-service. Falta definir como o barramento se autentica (mTLS, token de serviço) e restringir quem pode chamar.

Sobre dados pessoais: as mensagens de erro das malformadas vão para o log, e por isso as de CPF e CNPJ não repetem o documento, só o tamanho recebido. Os outros campos, como e2eId e ISPB, aparecem como vieram.
