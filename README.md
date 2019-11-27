# FEUP-AIAD

## MyBus

**Objetivo**

Tem-se como objetivo neste projeto, a criação de um sistema multi-agentes que irá ser utilizado para estudar a viabilidade de um sistema inovador de autocarros que se adaptam às necessidades dos passageiros. 

Em vez de seguirem um itinerário fixo, o percurso destes autocarros é determinado de acordo com os destinos dos passageiros que se encontram dentro do veículo. Também é possível que o autocarro altere o seu percurso para recolher novos passageiros, mas apenas se o desvio necessário para o fazer não for significativo. 

Quando um passageiro chega a uma certa paragem, é enviada uma mensagem aos autocarros que possuem esse local no seu itinerário, com as informações do passageiro. No caso de nenhum autocarro efetuar paragem no local, esta mensagem será enviada para todos os autocarros. 

O autocarro pode ou não aceitar o pedido dependendo do desvio do itinerário atual e da sua lotação.

Caso o autocarro termine o seu itinerário, este irá fazer um percurso aleatório na tentativa de recolher novos passageiros.


| Variáveis independentes   |  Variáveis dependentes |
| ------------------------- | ---------------------- |
| Localização das paragens  | Tempo de espera |
| Número de autocarros      | Tempo de viagem |
| Capacidade de autocarros | Ocupação do autocarro |
| Origem e destino de passageiros | Percurso do autocarro |

## TODO
- [ ] Inserir stop no meio do itinerário, em vez de ser sempre no fim (otimização do itinerário)

- [X] Itinerário aleatório quando fica sem stops (Inês)

- [ ] Não aceitar passageiro se o desvio for grande

- [X] Não verificar cegamente os lugares disponiveis. Ter em conta quando é que vão sair passageiros.

- [X] Implementar preço

- [X] Melhorar mapa

- [X] Percurso inverso do passageiro (1->3 3->1): atualmente o programa não permite que a mesma paragem seja inserida mais que uma vez, sendo que o segundo passageiro não chega ao destino

- [X] Mostrar estatisticas

- [X] Testar (cenários diferentes - variar nº e tipo de autocarros e passageiros)

## Aumento da complexidade das decisões

### Passageiro
- [X] alpha * preço + (1 - alpha) * tempo : fórmula para a escolha do autocarro

### Autocarro
- [X] preço por minuto
- [X] desonestidade 
- [ ] desvio
- [ ] ter em conta a lotação no cálculo do preço

## Estatíticas
- [X] Tempo médio de espera
- [X] Ocupação média
- [X] Desvio entre tempo prometido e tempo real
- [X] Ganho

## Apresentação

[Link](https://docs.google.com/presentation/d/1qO-rXMEMbW7mUmR5a3nyVEzetkSMrdGXcXtN5SL2d-k/edit?usp=sharing)

## Sugestões do professor

[] Negociação entre autocarro e passageiro

[] Colaboração

[] Testes automatizados

## Descrição da parte 2

Na primeira parte deste projeto, foi criado um sistema multi-agentes onde é possível estudar a viabilidade de um sistema inovador de autocarros que se adaptam às necessidades dos passageiros. Quando os passageiros chegam a uma paragem, é enviado um pedido aos autocarros para que estes o recolham. Estes irão enviar as suas propostas ao passageiro que irá escolher aquela que considera melhor. Os autocarros podem ter diferentes velocidades, preços e grau de desonestidade, enquanto os pasageiros podem ter diferentes *alpha*s (preferência pela redução do tempo em relação com a redução do preço).

Nesta segunda parte, irá ser "dada" mais inteligência aos passageiros. Estes irão guardar um registo de atraso dos autocarros, de maneira a descobrir quais são os desonestos.

### Problema 1
Até que ponto compensa aos autocarros serem desonestos, se os passageiros acabarem por se aperceber disto e preferirem os honestos?


