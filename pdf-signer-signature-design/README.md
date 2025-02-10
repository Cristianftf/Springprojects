# Fermina

## firmar pdf
- desencriptar la contrasenha
  - en caso de error lanzar error con codigo 500
- cargar el pkcs12
  - en caso de error lanzar error con codigo 400
- cargar el pdf
  - en caso de error lanzar error 400
- crear la firma
  - en caso de error lanzar error con codigo 500
- anhadir la firma al pdf
  - en caso de error lanzar error con codigo 500
- devolver el pdf firmado y codigo 200

## validar pdf
- cargar el pdf
  - en caso de error lanzar error con codigo 400
- obtener firmas del pdf
  - verificar todas las firmas del pdf
- devolver los detalles de las firmas y codigo 200

## validar pkcs12
- desencriptar la contrasenha
  - en caso de error lanzar error con codigo 500
- cargar el pkcs12
  - en caso de error lanzar error con codigo 400
- validar el certificado
- devolver especificacion de si es valido o no y codigo 200

## parsear pkcs12
- desencriptar la contrasenha
  - en caso de error lanzar error con codigo 500
- cargar el pkcs12
  - en caso de error lanzar error con codigo 400
- validar el certificado
- devolver especificacion de si es valido o no, detalles del certificado y codigo 200

## cambiar contrasenha del pkcs12
- desencriptar las contrasenhas
  - en caso de error lanzar error con codigo 500
- cargar el pkcs12
  - en caso de error lanzar error con codigo 400
- generar nuevo pkcs12 con la nueva contrasenha
- devolver nuevo pkcs12 y codigo 200