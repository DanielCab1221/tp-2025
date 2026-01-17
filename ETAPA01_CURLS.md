# TP 2025 - ETAPA 01
# CURLs para probar la API de user-svc

Requisitos:
- MySQL + phpMyAdmin arriba.
- `user-svc` corriendo en `http://localhost:8080`.

Notas:
- `idBanco` válido: 1 (ACME) o 2 (BWV RIO) según el seed.
- En PowerShell usar `curl.exe` y `^` para saltos de línea.

## Usuarios

### Crear huésped
```
curl.exe -X POST http://localhost:8080/users/huesped ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Juan Perez\",\"email\":\"juan@mail.com\",\"telefono\":\"3411234567\",\"dni\":\"30123456\",\"fechaNacimiento\":\"1990-05-15\",\"numeroCC\":\"4111111111111111\",\"nombreTitular\":\"JUAN PEREZ\",\"fechaVencimientoCC\":\"1227\",\"cvcCC\":\"123\",\"esPrincipalCC\":true,\"idBanco\":1}"
```

### Crear propietario
```
curl.exe -X POST http://localhost:8080/users/propietario ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Carlos Gomez\",\"email\":\"carlos@mail.com\",\"telefono\":\"3419998888\",\"dni\":\"27123456\",\"idHotel\":null,\"cuentaBancaria\":{\"numeroCuenta\":\"123456789\",\"cbu\":\"2850590940090418135201\",\"alias\":\"carlos.cbu\",\"idBanco\":1}}"
```

### Buscar usuarios por nombre (paginado)
```
curl.exe "http://localhost:8080/users?nombre=juan&page=0&size=10"
```

### Buscar usuario por DNI exacto
```
curl.exe "http://localhost:8080/users/dni/30123456"
```

### Buscar usuarios por DNI (contiene, paginado)
```
curl.exe "http://localhost:8080/users/buscar-dni?dni=30&page=0&size=10"
```
