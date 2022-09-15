# Programación de Objetos Distribuidos - TP1

## Autores

- [Azul Candelaria Kim](https://github.com/AzuCande)
- [Matias Lombardi](https://github.com/matiaslombardi)
- [Patrick M. Dey](https://github.com/patrickmdey)
- [Santos Rosati](https://github.com/srosati)
- [Uriel Mihura](https://github.com/uri-99)

---------------------------------------------------------------------------------

## Objetivo

El objetivo de este Trabajo Práctico es implementar un sistema de tickets de vuelos con concurrencia y multithreading.
Más información sobre la consigna en el pdf encontrado en este mismo directorio.

---------------------------------------------------------------------------------

## Estructura del proyecto
Como visto en clase, la estructura de este proyecto está dividida en 3 partes:
Client - Server - Api

### Api
Contiene los modelos, excepciones e interfaces que deberán ser utilizados y/o implementados por ambos Client y Server

### Server
Contiene la implementación de los servicios ofrecidos al Client

### Client
Contiene parsers necesarios para envíar los pedidos al Server


---------------------------------------------------------------------------------

## Compilación y ejecución
Antes de comenzar a compilar y ejecutar el proyecto, modifique la variable TP_PATH en el archivo `compile.sh` con la ruta absoluta del directorio donde se encuentra el proyecto.
Luego, ejecute:
```bash
> ./compile.sh
```

### Server
Para ejecutar el servidor, ubíquese en el directorio `tpe1-g6-server-1.0-SNAPSHOT` y ejecute:
```bash
> run-registry.sh
> run-server.sh
```
El servidor se ejecutará en el puerto 1099.

### Client
Para ejecutar cualquiera de los clientes, ubíquese en el directorio `tpe1-g6-client-1.0-SNAPSHOT` y ejecute alguno de los scripts.
Para todos se debe pasar como argumento `-DserverAddress=xx.xx.xx.xx:yyyy` donde `xx.xx.xx.xx` es la dirección IP del servidor y `yyyy` es el puerto en el que se ejecuta el servidor.

#### Cliente de Administración de Vuelos

```bash
> run-admin-client -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -DinPath=filename | -Dflight=flightCode ]
```
donde los posibles valores del `actionName` son:
  * `models` agrega un lote de modelos de aviones.
    * `filename` contiene la ruta al archivo CSV que contiene los modelos de aviones.
  * `flights` agrega un lote de vuelos.
    * `filename` contiene la ruta al archivo CSV que contiene los vuelos.
  * `status` consulta el estado del vuelo de código`flightCode`.
  * `confirm` confirma el vuelo de código `flightCode`.
  * `cancel` cancela el vuelo de código `flightCode`.
  * `reticketing` fuerza el cambio de tickets de vuelos cancelados por tickets de vuelos alternativos.

#### Cliente de Asignación de Asientos

```bash
> run-seatAssign -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -Dflight=flightCode [ -Dpassenger=name | -Drow=num | -Dcol=L | -DoriginalFlight=originFlightCode ]
```

donde los posibles valores del `actionName` son:
  * `status` imprime en pantalla si el asiento de fila `num` y columna `L` del vuelo `flightCode` está disponible o no.
  * `assign` asigna al pasajero `name` al asiento libre de fila `num` y columna `L` del vuelo de código `flightCode`.
  * `move` mueve al pasajero `name` de un asiento asignado en el vuelo de código `flightCode` a un asiento libre del mismo vuelo, ubicado en la fila `num` y columna `L`.
  * `alternatives` lista los vuelos alternativos al vuelo de código `flightCode` para el pasajero `name`.
  * `changeTicket` cambia el ticket del pasajero `name` del vuelo de código `originFlightCode` al vuelo alternativo de código `flightCode`.

#### Cliente de Notificaciones del Vuelo
    
```bash
> run-notifications -DserverAddress=xx.xx.xx.xx:yyyy -Dflight=flightCode -Dpassenger=name
```

donde `flightCode` es el código de vuelo y `name` es el nombre del pasajero.

#### Cliente de Consulta del Mapa de Asientos

```bash
> run-seatMap -DserverAddress=xx.xx.xx.xx:yyyy -Dflight=flightCode [ -Dcategory=catName | -Drow=rowNumber ] -DoutPath=output.csv
```

donde:
* `flightCode` es el código del vuelo.
* `output.csv` es el archivo CSV donde se escribirá el mapa de asientos.
* Si no se indica `-Dcategory` ni `-Drow`, se imprime en pantalla el mapa de asientos completo del vuelo.
* Si se indica `-Dcategory`, se imprime en pantalla el mapa de asientos de la categoría `catName` del asiento del vuelo elegido.
* Si se indica `-Drow`, se imprime en pantalla el mapa de asientos de la fila `rowNumber` del vuelo elegido.
