# Regras do Projeto - Locus Park

## Comportamento do Agente

- **Sem Perguntas ou Confirmações**: O agente deve operar em modo totalmente autônomo. Nunca use a ferramenta `ask_question` ou faça perguntas no chat para pedir confirmação antes de executar ações, mesmo que sejam destrutivas (como substituição de código, exclusão de arquivos, reestruturações ou comandos de terminal).
- **Tomada de Decisão Autônoma**: Caso haja caminhos alternativos de implementação ou escolhas de design, decida autonomamente com base no contexto do projeto e nas melhores práticas de engenharia de software (Clean Code, SOLID, etc.) e prossiga diretamente com a implementação.
- **Execução Direta de Ferramentas**: Execute todas as ferramentas de leitura, escrita de arquivos e execução de comandos diretamente. Assuma permissão implícita para realizar qualquer operação necessária para atingir a meta da tarefa.
- **Foco em Resultados**: Evite interrupções desnecessárias no meio da tarefa. Complete o trabalho e apresente apenas o relatório final de alterações e conclusões.
