# JSON Web Token Generálása és Szűrése
## Mi is az a JWT
* A JWT (JSON Web Token) egy nyílt szabvány alapú token formátum, amelyet gyakran használnak az azonosítás és az adatok hitelesítése céljából webalkalmazásokban. A JWT egy kompakt, önálló és biztonságos módszer az információk cseréjére a felhasználók között JSON formátumban.

* A JWT három részből áll, amelyeket ponttal választanak el egymástól: a fejlécből (header), a törzsből (payload) és az aláírásból (signature).

* A fejléc tartalmazza a token típusát (amely JWT), valamint a használt algoritmust az aláírás elkészítéséhez.

* A törzs vagy a payload tartalmazza az információkat vagy az adatokat, amelyeket a token hordoz. Ez lehetnek a felhasználó azonosítója, a szerepköre, lejárati idő vagy más egyedi adatok.

* Az aláírás a token hitelesítésére szolgál. Az aláírás elkészítéséhez használnak egy titkos kulcsot (vagy publikus/privát kulcspárt), amely csak a szerveren ismert. Az aláírásban szereplő adatokhoz tartozó hash értéket hasonlítják össze a szerveren a token érvényességének ellenőrzésekor.

### A JWT-t általában a következő módon használják:

1. A felhasználó sikeresen bejelentkezik a rendszerbe, és a szerver kibocsát egy JWT-t válaszként.
2. A kliens minden további kérésében elküldi a JWT-t az Authorization fejlécben vagy más módon.
3. A szerver ellenőrzi az érkező JWT-t, megbizonyosodik arról, hogy nem módosították, és érvényes-e a lejárati időre és az aláírásra nézve.
4. Ha a JWT érvényes, a szerver feldolgozza a kérését, megbízható információkat tartalmazva a JWT törzsében. 

A JWT előnye, hogy könnyen hordozható és önálló, tehát nincs szükség kliens- és szerveroldali állapotkövetésre. Ezenkívül a JWT-k biztonságosak lehetnek, ha megfelelően védik a titkos kulcsot és a token érvényességét ellenőrzik. A JWT használata elterjedt az API autentikációban és az egyszerűsített hozzáféréskezelésben (SSO) alkalmazásokban.










# ELŐZMÉNYEK: Autentikáció adatbázis használatával

Ez a leírás a korábbi regisztrációs projektre épül, aminek a leírásást az előzzmények szekcióban találod.

### UserDetailsService implementálása

1. A SecurityConfig osztályba vegyél fel egy új változót (CustomUserDetailsService)
2. Hozd létre a CustomUserDetailsService osztályt (@Service), ami implementálni fogja a UserDetailsService interfészt
    * Legyen hozzáférése az AccountRepositroryhoz
    * Konstruktor injektálás
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