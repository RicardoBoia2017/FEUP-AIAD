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

- [ ] Não aceitar passageiro se o desvio for grande

- [ ] Testar (cenários diferentes - variar nº e tipo de autocarros e passageiros)

## Aumento da complexidade das decisões

### Autocarro
- [ ] desvio
- [ ] ter em conta a lotação no cálculo do preço

## Estatíticas

- [ ] exportação para CSV de cada passageiro

## Apresentação

[Link](https://docs.google.com/presentation/d/1qO-rXMEMbW7mUmR5a3nyVEzetkSMrdGXcXtN5SL2d-k/edit?usp=sharing)

## Sugestões do professor

- [x] Negociação entre autocarro e passageiro

- [x] Colaboração

- [x] Testes automatizados

## Descrição da parte 2

Na primeira parte deste projeto, foi criado um sistema multi-agentes onde é possível estudar a viabilidade de um sistema inovador de autocarros que se adaptam às necessidades dos passageiros. Quando os passageiros chegam a uma paragem, é enviado um pedido aos autocarros para que estes o recolham. Estes irão enviar as suas propostas ao passageiro que irá escolher aquela que considera melhor. Os autocarros podem ter diferentes velocidades, preços e grau de desonestidade, enquanto os pasageiros podem ter diferentes *alpha*s (preferência pela redução do tempo em relação à redução do preço).

Nesta segunda parte, os passageiros irão ter um maior grau de inteligência. Estes irão guardar um registo do atraso dos autocarros, de maneira a descobrir quais são os desonestos em relação ao tempo de duração da viagem.

### Problema 1

Será que o autocarro vai cumprir com o tempo prometido? (Classificação)

### Problema 2

Qual vai ser o atraso do autocarro? (Regressão)

## Variável dependente

Atraso dos autocarros

## Variáveis independentes

### Autocarro

Velocidade, preço, grau de desonestidade

### Passageiro

*alpha* (preferência pela redução do tempo em relação à redução do preço)
