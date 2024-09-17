# User

## Register User

Method : POST

Endpoint : /api/user

Request Body : 
```json
{
  "username" : "ken@example.com",
  "password" : "password",
  "name" : "ken"
}
```

Response Body (success) : 
```json
{
  "status" : true,
  "message" : "Success create user"
}
```

Response Body (failed) : 
```json
{
  "status" : false,
  "message" : "Field ?? is not empty"
}
```

## Login User 

Method : POST

Endpoint : /api/auth/login

Request Body :
```json
{
  "username" : "ken@example.com",
  "password" : "password"
}
```

Response Body (success) :
```json
{
  "status" : true,
  "message" : "Success create user",
  "data" : {
    "token" : "{token}",
    "expiredAt" : 234141414134 // milisecond
  }
}
```

Response Body (failed) :
```json
{
  "status" : false,
  "message" : "Field ?? is not empty"
}
```

## Get User

Method : GET

Endpoint : /api/users/current

Response Body (success) :
```json
{
  "status" : true,
  "message" : "Success create user",
  "data" : {
    "username" : "ken@example.com",
    "name" : "ken"
  }
}
```

Response Body (failed) :
```json
{
  "status" : false,
  "message" : "Field ?? is not empty",
  "errors" : "Unauthorized"
}
```

## Update User

Method : GET

Endpoint : /api/users/current

Request Header : 
```
- X-API-TOKEN : {TOKEN} (Mandatory)
```

Request Body : 
```json
{
  "password" : "test",
  "name" : "ken loj"
}
```

Response Body (success) :
```json
{
  "status" : true,
  "message" : "Success create user",
  "data" : {
    "username" : "ken@example.com",
    "name" : "ken"
  }
}
```

Response Body (failed) :
```json
{
  "status" : false,
  "message" : "Field ?? is not empty"
}
```

## Logout User

Method : GET

Endpoint : /api/users/current

Request Header :
```
 X-API-TOKEN : {TOKEN} (Mandatory)
```

Response Body (success):
```json
{
  "status" : false,
  "message" : "Field ?? is not empty"
}
```
