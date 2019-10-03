# FEUP-AIAD

Bus (..)

**Objetivo**

Tem-se como objetivo neste projeto, a criação de uma simulação baseada em agentes para estudar um sistema inovador de autocarros que se adaptam às necessidades dos passageiros. 

Em vez de seguirem um itinerário fixo, o percurso destes autocarros é determinado de acordo com os destinos dos passageiros que se encontram dentro do veículo. Também é possível que o autocarro altere o seu percurso para recolher novos passageiros, mas apenas se o desvio necessário para o fazer não for significativo. 

Quando um passageiro chega a uma certa paragem, é enviada uma mensagem aos autocarros que possuem esse local no seu itinerário, com as informações do passageiro. No caso de nenhum autocarro efetuar paragem no local, esta mensagem será enviada para todos os autocarros. 

O autocarro pode ou não aceitar o pedido dependendo do desvio do itenerário atual e da sua lotação.

Caso o autocarro termine o seu itinerário, este irá faz um percurso aleatório na tentativa de recolher novos passageiros.


**Váriaveis independentes**

 Localização das paragens
 
 Número de autocarros
 
 Capacidade de autocarros
 
 Origem e destino de passageiros
 
 
 **Variaveis dependentes**
 
  Tempo de espera
  
  Tempo de viagem
  
  Ocupação do autocarro
  
  Percurso do autocarro
