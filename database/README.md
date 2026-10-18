# Modelo de dados

`modelo_relacional.sql` cria as 7 tabelas do sistema no Oracle, com 50 restrições nomeadas e comentários em todas as tabelas e colunas.

| Classe Java | Tabela |
|---|---|
| `Estacao` | `ESTACAO` |
| `EscadaRolante` | `ESCADA_ROLANTE` |
| `DispositivoIoT` | `DISPOSITIVO_IOT` |
| `Leitura` | `LEITURA` |
| `Alerta` | `ALERTA` |
| `Equipe` | `EQUIPE` |
| `OrdemServico` | `ORDEM_SERVICO` |

Relacionamentos: `ESTACAO` 1:N `ESCADA_ROLANTE` 1:N `LEITURA` 1:N `ALERTA` 1:0..1 `ORDEM_SERVICO` N:1 `EQUIPE` N:1 `ESTACAO`; `ESCADA_ROLANTE` 1:N `DISPOSITIVO_IOT`.

Para visualizar no Oracle SQL Developer Data Modeler: **File → Import → DDL File** e escolha este script.

Enumerações do Java são gravadas pelo nome da constante e protegidas por `CHECK`; Value Objects viram colunas. As faixas físicas dos sinais são provisórias até a análise do dataset da Motiva.
