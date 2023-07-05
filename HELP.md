# JSON Web Token Generálása és Szűrése

## Hasznos linkek
Ebben a [videóban](https://www.youtube.com/watch?v=7Q17ubqLfaM&t=23s) röviden és érthetően összefoglalják, hogy
* mi a különbség a session és a token között,
* hogyan működik
* miért érdemes használni

A teljes autentikációs folyamathoz pedig ajánlom Teddy Smith csatornájáról a [Spring Bott Security Course](https://www.youtube.com/playlist?list=PL82C6-O4XrHe3sDCodw31GjXbwRdCyyuY) lejátszási listáját, ami nekem is segítségemre volt.

## Mi is az a JWT
* A JWT (JSON Web Token) egy nyílt szabvány alapú token formátum, amelyet gyakran használnak az azonosítás és az adatok hitelesítése céljából webalkalmazásokban. A JWT egy kompakt, önálló és biztonságos módszer az információk cseréjére a felhasználók között JSON formátumban.

* A JWT három részből áll, amelyeket ponttal választanak el egymástól: a fejlécből (header), a törzsből (payload) és az aláírásból (signature).

* A fejléc tartalmazza a token típusát (amely JWT), valamint a használt algoritmust az aláírás elkészítéséhez.

* A törzs vagy a payload tartalmazza az információkat vagy az adatokat, amelyeket a token hordoz. Ez lehetnek a felhasználó azonosítója, a szerepköre, lejárati idő vagy más egyedi adatok.

* Az aláírás a token hitelesítésére szolgál. Az aláírás elkészítéséhez egy titkos kulcsot (vagy publikus/privát kulcspárt) használnak, amely csak a szerveren ismert. Az aláírásban szereplő adatokhoz tartozó hash értéket hasonlítják össze a szerveren a token érvényességének ellenőrzésekor.

### A JWT-t általában a következő módon használják:

1. A felhasználó sikeresen bejelentkezik a rendszerbe, és a szerver kibocsát egy JWT-t válaszként.
2. A kliens minden további kérésében elküldi a JWT-t az Authorization fejlécben vagy más módon.
3. A szerver ellenőrzi az érkező JWT-t, megbizonyosodik arról, hogy nem módosították, és érvényes-e a lejárati időre és az aláírásra nézve.
4. Ha a JWT érvényes, a szerver feldolgozza a kérését, megbízható információkat tartalmazva a JWT törzsében. 

A JWT előnye, hogy könnyen hordozható és önálló, tehát nincs szükség kliens- és szerveroldali állapotkövetésre. Ezenkívül a JWT-k biztonságosak lehetnek, ha megfelelően védik a titkos kulcsot és a token érvényességét ellenőrzik. A JWT használata elterjedt az API autentikációban és az egyszerűsített hozzáféréskezelésben (SSO) alkalmazásokban.


## I. Belépési pont
1. Hozz létre egy osztályt (JWTAuthEntryPoint) ami implementálja az AuthenticationEntrypoint interfészt
2. Valósítsd meg az interfész metódusát:
   * a commence() metódus a hitelesítési hibák kezelésére szolgál, és a válaszüzenetben visszajelzést ad a kliensnek arról, hogy az azonosítás sikertelen volt és nincs jogosultsága a kért erőforrás eléréséhez.
   

   ```response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());```
   * 401-es választ küld az getMessage()-ben tárolt üzenettel (módosítható).

## II. SecurityConfig módosítások
1. Add hozzá a fieldekhez a JWTAuthEntryPoint osztályt, és bővítsd ennek megfelelően a konstruktort
2. Bővítsd a SecurityFilterChaint a kivételkezeléssel, és add hozzá a belépési pontot, valamint a sessionCreationPolicy legyen STATELESS
    ```.exceptionHandling().authenticationEntryPoint(authEntryPoint).and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()```

## III. JWT Generátor
1. Hozz létre egy osztályt (JWTGenerator), tedd komponensé (@Component)
2. Legyen egy végleges statikus változója, ami konzisztensen elérhetővé teszi a titkos kulcsot:
   ```private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);```

* Az HS512 algoritmus a JSON Web Token (JWT) aláírásához használt titkosítási algoritmus. A Keys.secretKeyFor(SignatureAlgorithm.HS512) metódus létrehoz egy új, véletlenszerűen generált kulcsot a megadott algoritmussal. Ez a kulcs szükséges ahhoz, hogy az elkészített JWT-ket aláírjuk, hogy biztosítsuk az adatok integritását és hitelességét.
* **A gyakorlatban javasolt lehetőség az ilyen érzékeny adatok külső konfigurációs fájlokban vagy környezeti változókban történő tárolása, hogy könnyen lehessen őket cserélni vagy rejtetten tárolni.**

3. Készítsd el a metódusokat, melyekkel legyártod és validálod a tokent, valamint amivel kinyered a felhasználónevet:
    #### generateToken(Authentication authentication) : String
* Az authentication-től kérd el a felhasználó nevét
* Mentsd el az éppen aktuális dátumot, és határozd meg a lejárat idejét (ezt akár egy külön osztályban is megteheted, hogy együtt gyűjtsd a hasonló konstansokat)
* Add meg a kulcsot és az aláírási algoritmust
* Végül foglald egybe az eredményt, ami egyben a visszatérési érték
 
#### validateToken(String token) : boolean
* Dekódold a tokent -> parserBuilder()
* Add meg a kulcsot -> setSigningKey(key)
* Hozd létre a JWT parser objektumot -> build()
* Ellenőrizd az aláírást -> parseClaimsJws(token)
* Térj vissza true értékkel (Ha az aláírás nem egyező, akkor exception)

#### getUsernameFromJWT(String token) : String
* Ugyanúgy dekódolni kell a tokent a kulccsal, létre kell hozni a parser objektumot és össze kell hasonlítani az aláírást
* Ezt követően kérd ki a JWT claims részét -> getBody()
* Majd abból a felhasználónevet -> getSubject()


## IV. JWTAuthenticationFilter
1. Származtasd le a OncePerRequestFilter osztályból, hogy kérésenként csak egyszer legyen végrehajtva a szűrés
2. Hozd be a JWTGenerator és a CustomUserDetailsService osztályt (Ügyelj rá, hogy legyen üres konstruktor is, ha konstruktoron keresztül megy az injection)
3. Valósítsd meg a `doFilterInternal` metódust. (Ellenőrzi, hogy van-e token a headerben)
   1. `getJWTFromRequest(request)` metódus meghívása, hogy kinyerje a JWT tokent az HTTP kérésből. Ezt a metódust neked kell megírni oly módon, hogy kinyerd az Authorization fejlécből a tokent ("Bearer " előtag nélkül) hogy aztán azzal, vagy hiánya esetén nullal visszatérhess
   2. Az `StringUtils.hasText(token)` metódus ellenőrzi, hogy a token nem üres és tartalmaz valamilyen szöveges értéket.
   3. `tokenGenerator.validateToken(token)` metódushívással a tokenGenerator objektum validálja a tokent. Ez az ellenőrzés meghatározza, hogy a token érvényes-e, például ellenőrzi az aláírást, az érvényességi időt stb.
   4. Ha a token érvényes, akkor a `tokenGenerator.getUsernameFromJWT(token)` metódus meghívása történik, hogy kinyerje a felhasználónevet a JWT tokentől.
   5. `customUserDetailsService.loadUserByUsername(username)` metódushívással a customUserDetailsService objektum betölti a felhasználó részleteit a felhasználónév alapján. Ez az objektum felelős a felhasználói adatok kezeléséért, például az adatbázisból vagy más forrásból való betöltésért.
   6. A `UsernamePasswordAuthenticationToken` objektum létrehozása az authentikációhoz szükséges. A userDetails paraméter az előző lépésben betöltött felhasználói részleteket tartalmazza, a null a jelszót jelenti (mivel JWT token esetén nincs szükség jelszóval való autentikációra), és a userDetails.getAuthorities() visszaadja a felhasználó jogosultságait.
   7. Az `authenticationToken.setDetails(...)` metódushívással beállítja az authenticationToken részleteit, például a WebAuthenticationDetailsSource-ból előállított részleteket.
   8. `SecurityContextHolder.getContext().setAuthentication(authenticationToken)` metódushívással beállítja az aktuális felhasználó hitelesítését a SecurityContextHolder-ben, így az alkalmazás többi részében elérhetővé válik.
   9. `filterChain.doFilter(request, response)` meghívásával továbbítja a kérést és a választ a következő szűrőlánc elemnek.

## V. SecurityConfig
1. AJTAuthenticationFilter osztályt tedd Bean-né
2. A szűrőláncba szúrd be az elkészített filtert: `http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);`

A fenti kódban a `http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)` kifejezés azt jelenti,
hogy a `jwtAuthenticationFilter()` metódus által létrehozott szűrőt beszúrjuk a szűrőláncba a `UsernamePasswordAuthenticationFilter` osztályú szűrő előtt.


## VI. AuthResponseData
Ez egy egyszerű DTO lesz (@Data), ami két Stringet tartalmaz:
1. accessToken
2. tokenType = "Bearer"
Ezen felül egy konstruktort.

## VII. AccountService
1. Add hozzá a fieldekhez és a konstruktort is bővítsd a JWTGenerator osztállyal
2. Írd meg a `login(LoginCommand loginCommand)` metódust:
   - Az `authenticationManager.authenticate(...)` metódushívással az authenticationManager objektum elvégzi az autentikációt a megadott felhasználónév és jelszó alapján.
   Ez a lépés az authentikációs folyamatot végzi, például ellenőrzi a felhasználónév-jelszó párost az alkalmazás biztonsági rendszerében vagy más autentikációs forrásban.
   Az autentikációs eredményt az authentication változóban tároljuk.

   - A `SecurityContextHolder.getContext().setAuthentication(authentication)` metódushívással beállítjuk az aktuális felhasználó hitelesítéséta SecurityContextHolder-ben.
   Ez azt jelenti, hogy a bejelentkezett felhasználó az alkalmazás többi részében hozzáférhető lesz az autentikációs információkkal.

    - A `jwtGenerator.generateToken(authentication)` metódushívással generálunk egy JWT tokent az autentikációs objektumból. A jwtGenerator objektum felelős a JWT tokenek generálásáért
   és az autentikációs adatok tokenbe ágyazásáért.

   - Végül a generált JWT tokent visszaadja, amelyet általában a kliens (például egy webalkalmazás vagy mobilalkalmazás) tárol, és a későbbi kérésekben
   a felhasználó azonosítására és hitelesítésére használja.

## VIII. AccountController
Itt már csupán meg kell hívni a szervíz rétegből a `login()` metdust a request body-ból lekért LoginCommanddal,
és gondoskodni kell róla, hogy visszaadjuk a tokent. Ehhez:
- A `ResponseEntity`-ben egy `AuthResponseData` objektumot küldünk visza, amit a generált token alapján hozunk létre.
- (A szervíz réteg login metódusa a tokennel tér vissza , így azt el tudod menteni egy Stringbe).

-----------------------------------------------------------------------------------
# ELŐZMÉNYEK: Autentikáció adatbázis használatával

Ez a leírás a korábbi regisztrációs projektre épül, aminek a leírásást az előzzmények szekcióban találod.

### UserDetailsService implementálása

1. A SecurityConfig osztályba vegyél fel egy új változót (CustomUserDetailsService)
2. Hozd létre a CustomUserDetailsService osztályt (@Service), ami implementálni fogja a UserDetailsService interfészt
    * Legyen hozzáférése az AccountRepositroryhoz
    *** Konstruktor injektálás**
    * Írd felül a loadUserByUsername(String username) metódust
        * Hozz létre egy Accountot -> a repóból keresd ki a username alapján (ha nincs ilyen lekérdezésed még, ami
          Accounttal térne vissza, akkor azt is készítsd el)
        * Térj vissza egy új Userrel! Paraméternek add meg az accountból kinyert felhasználónevet, jelszót, és a
          szerepköröket!
          <br>A szerepköröknél a User nem fogad List<Role> típusú adatot!</br>
        * Meppeld át Collettion<GrantedAuthority> típusra egy privát metódusban
        * Harmadik paraméternek hívd meg ezt a metódust a korábban megírt User konstruktorában

### SecurityConfig módosítások

* Add hozzá osztályváltozóként a CustomUserDetailsService osztályt
* Konstruktor injektálás
* A filter chainnél engedélyezd a "/api/accounts/login" végpontot is
* Hozz létre egy Beant az AuthenticationManagerből
    * Paraméterként AuthenticationConfiguration
    * Visszatérésként: authenticationConfiguration.getAuthenticationManager();

### Account Service módosítások

* A login metódus paraméterként kapjon egy LoginCommand objektumot (tartalmazza a felhasználó által megadott
  bejelentkezési adatokat)
* A bejelentkezés során a metódus hozzon létre egy UsernamePasswordAuthenticationToken objektumot, amelyben átadja a
  felhasználó által megadott felhasználónevet és jelszót. (Ez a token azonosítja majd a bejelentkező felhasználót)
* Ezt követően a authenticationManager (a Spring Security által biztosított komponens) segítségével autentikáld a
  felhasználót
* Az authenticate metódus visszatérési értéke egy Authentication objektum, amely reprezentálja a sikeres bejelentkezést
* A SecurityContextHolder.getContext().setAuthentication(authentication) kóddal állítsd be az autentikációt a
  SecurityContextHolder-ben

### AccountController módosítások

* Hozd létre a PostMapping-et a /login végponton
* A @RequestBody annotációval kérde be az adatokat LoginCommand formában
* Hívd meg a szervíz réteg login(LoginCommand loginCommand) metódusát
* Térj vissza 200-as kóddal
* _Megjegyzés: A szervíz rétegben a login metóduson belül az authenticate() metódus elvégzi a hitelesítést, és hibás
  jelszó vagy felhasználónév esetén
  AuthenticationExceptiont dob, aminek eredményeként a controller 401-es kódot küld a kliensnek válaszul. Ha a válasz
  String formában is meg kell érkezzen, akkor foglald a hívást try/catch blokkba és a catch ágon a kívánt formában
  küldheted a választ_

# ELŐZMÉNYEK: Regisztráció implementálása Spring Boot és WebSecurity használatával

## <u>1. Alapok</u>

### Maven Függőségek

Az alábbi függőségekre lesz szükség:

* Spring Web
* JPA Data
* MySQL Driver
* Spring Security

### Konfiguráció

0. Az <b>application.yaml</b> fájlban konfiguráld a server és az adatbázis kapcsolatot, valamint a JPA viselkedését és a
   log szintet!
1. Hozz létre egy SecurityConfig osztályt (@Configuration @EnableWebSecurity) és készíts Bean-eket az alábbiakból:
    * SecurityFilterChain
        * Köss mindent autentikációhoz, és egy felsőbb szinten határozd meg, hogy a /register végpontot mindenki
          elérhesse
    * UserDetailService
        * Definiáld a ROLE-okat és térj vissza egy új InMemoryUserDetailsManagerrel
        * A role adattáblába vidd fel a definiált ROLE-okat, különben a kérés küldésekor hibát kapsz!
    * PasswordEncoder
        * Térj vissza egy új BCryptEncoderrel

### Domian, DTO, Repo

##### Account tulajdonságok:

* id: Long
* username: String
* password: String
* email: String
* roles: List<Role>

Entitásként annotáld és generálj táblát belőle. Az @Data annotáció gondoskodik a getterekről, setterekről és a
konstruktorról.
A listánál gondoskodj a many2many (vagy egy on2many és egy many2one) kapcsolatról!

##### AccountRepository, RoleRepository

* Interfész, amit a JpaRepository<T, T> interfészből származtass le. (<entitás, elsődleges kulcs adattípusa>)
* Implementáld a szükséges lekérdezéseket (figyelj a beépített metódusokra)

##### AccountCommand

Ez egy bejövő DTO lesz, ebben határozd meg, hogy milyen adatokat kérsz be a felhasználótól.
Használd az @Data annotációt az osztályra!

### Controller

* Használd a @RestController és a @RequestMapping annotációkkal!
* Legyen kapcsolata a szervíz réteggel!
* konstruktor injektálás

Ebben az osztályban figyeld, hogy érkezik-e POST kérés a /register végpontra, kérd be a request body-t AccountCommand
formában
és dobd tovább a tennivalókat a szervíz rétegnek, majd térj vissza egy 201-es, vagy 400-as kóddal!

### Service

* Használd az @Service és a @Transactional annotációkat az osztályra
* Építs kapcsolatot a repositorykkal, és a pwencoderrel
* Konstruktor injektálás
* Valósítsd meg a regisztáció szabályait
    * Egyelőre elég ha megnézed, hogy létezik-e az adatbázisban a felhasználónév vagy az email cím
    * Létező account esetén dobj valamilyen hibát és kezeld a kontoller osztályban
    * Meppeld át a bejövő dto-t Accountra, de a jelszót hasheld el!
    * Mentsd el az accountot az adatbázisba

### Eredmény

Ezen a ponton az adatbázisba el tudod menteni azokat az adatokat, amiket a felhasználó a register végpontról küld, úgy
mint felhasználónév, email cím és jelszó.

### Folytatás

Bejelentkezés kezelése: autentikáció adatbázisból