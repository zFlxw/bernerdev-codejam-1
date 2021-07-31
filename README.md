# Bernerdev CodeJam - Login Page

![img](https://cdn.discordapp.com/attachments/794300974247575604/870274118023524402/bernerdev.logo..png)

## How to run
1. create `banned_ips.properties` and `data.properties` in your run directory
2. start the http server (run the main method in `LoginScreen`)
3. head over to `http://localhost:8080`
   1. there is no index page, if you are logged in you will get redirected to the ``dashboard``
   2. if you are not logged in, you will get redirected to the ``login`` page
4. You can register on the login page by clicking on ``Register instead?`` or by entering `http://localhost:8080/register`

- the dashboard will display, whether you are logged in or not.

## Topic and task

> Create a secure web application with a login, registration and at least one protected page.

- store password and user data
- passwords should not get stored in plaintext
- no ajax, no rest
- if a user fails three times giving in a password, his ip should get blocked

## Deadline

> 31. Juli 2021, 24:00 CEST

## Rules

- only native language libraries are allowed, no third party libs (exception: Molecule for Java/Kotlin)
- using external services is not allowed
- downloading libraries and/or code is not allowed, the code must be self written
- source code must get uploaded into a public repo until deadline

## Organizer

Thanks to Bernerdev.de for organizing and managing this coding jam. If you want to take part in future jams, join our discord: [https://discord.gg/EGHy5tXBnj](https://discord.gg/EGHy5tXBnj)