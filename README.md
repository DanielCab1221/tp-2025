# tp-2025
Trabajo práctico DAN 2025

## Herramientas necesarias

### IDE
- VSCode: https://code.visualstudio.com/ 
- Brackets *IDE para frontend* https://brackets.io/
- Phoenix Code *IDE para frontend* https://phcode.dev/

### Entornos de ejecucion backend
- Java 21: https://adoptium.net/es/temurin/releases/?version=21&package=jdk 
- Docker Desktop: https://docs.docker.com/desktop/setup/install/windows-install/

### Entornos de ejecucion frontend
- Node 22: https://nodejs.org/es/download 

### Gestión de código
- Git: https://git-scm.com/
- Usuario en Github: https://github.com/ 


# Organizacion de directorios

Este proyecto es un MONOREPO (es decir tendremos todos los elementos necesarios para ejecutar la aplicacion distribuida en un único repositorio)

## Infra
Contiene los archivos docker para iniciar los servicios de infraestructura y los 3 microservicios:
- MySQL + phpMyAdmin (base de datos de `user-svc`)
- PostgreSQL + pgAdmin (base de datos de `gestion-svc`)
- MongoDB + mongo-express (base de datos de `reservas-svc`)
- RabbitMQ (mensajería asíncrona entre `gestion-svc` y `reservas-svc`)

### Levantar y bajar la infra + los servicios

Para levantar todo el stack (requiere haber compilado antes, ver [Ejecución](#ejecucion)):
```
docker compose -f infra/docker-compose.yml up -d --build
```

Para levantar solo una parte, por ejemplo MySQL + phpMyAdmin:
```
docker compose -f infra/docker-compose.yml up -d mysql phpmyadmin
```

Para bajar ***todos*** los servicios de docker-compose
```
docker compose -f infra/docker-compose.yml down
```
Para bajar y borrar los datos de ***todos*** los servicios de docker-compose
```
docker compose -f infra/docker-compose.yml down -v
```

## Services
Microservicios Spring Boot (Java 21):
- **user-svc** (`services/user-svc`, puerto `8081`): usuarios (huéspedes/propietarios), tarjetas de crédito, bancos.
- **gestion-svc** (`services/gestion-svc`, puerto `8083`): hoteles, habitaciones, tipos de habitación, tarifas.
- **reservas-svc** (`services/reservas-svc`, puerto `8082`): búsqueda de disponibilidad y gestión de reservas.

Comparten la librería `common/dan-common-lib` con los DTOs usados en la mensajería entre `gestion-svc` y `reservas-svc`.

## Frontend
`frontend/` es un panel en React + Vite + TypeScript para probar visualmente los 3 microservicios (no es parte del enunciado del TP, es una herramienta de testing). Para levantarlo, con el backend ya corriendo:
```
cd frontend
npm install
npm run dev
```
Por defecto apunta a `localhost:8081/8082/8083`; las URLs son configurables en `frontend/.env`.

# Ejecucion

Para compilar los 3 servicios y la librería común desde la raíz del repo (necesario antes de `docker compose ... --build`):
```
./mvnw clean install -DskipTests
```

Para levantar un servicio individual en modo desarrollo, por ejemplo `user-svc` (requiere la infra correspondiente levantada, y usa `application-local.properties` para conectarse a `localhost` en vez de a los hostnames de Docker):
```
cd services/user-svc
./mvnw spring-boot:run -DskipTests -Dspring-boot.run.profiles=local
```

## Enunciado 

### TP Etapa 01
- Tareas necesarias para completar el servicio user-svc [ETAPA01.md](./ETAPA01.md).

### TP Etapa 02
- Tareas necesarias para completar el servicio gestion-svc y reservas-svc [ETAPA02.md](./ETAPA02.md).
- Descripción paso a paso de las acciones a realizar [PRACTICA_02.pdf](PRACTICA_02.pdf)
