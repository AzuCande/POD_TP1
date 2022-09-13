# Programación de Objetos Distribuidos - TP1

---------------------------------------------------------------------------------
links que fueron agregando:

https://www.baeldung.com/java-concurrent-hashset-concurrenthashmap

https://stackoverflow.com/questions/48183999/what-is-the-difference-between-putifabsent-and-computeifabsent-in-java-8-map#:~:text=computeIfAbsent%20will%20not%20put%20a%20null%20value%20if,a%20difference%20to%20calls%20like%20getOrDefault%20and%20containsKey.

---------------------------------------------------------------------------------

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
```bash
mvn clean
run registry
run server
```

```bash
levantar planes
levantar flights
run-admin.sh ... -Daction=assign ...
```

---------------------------------------------------------------------------------

## Testeo

---------------------------------------------------------------------------------

