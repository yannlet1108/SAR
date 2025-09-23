 # SPEC Générale 
 
 
 Ce package a pour but de permettre à différentes tâches d'échanger des données via un canal de communication mis en place par un broker.


 ## Task
 Les Task sont définies avec un Broker (pour communiquer entre différentes tâches) et un Runnable (programme à exécuter).
 Toutes les Task d'une même JVM possèdent le même Broker.
 Elles peuvent par exemple servir à définir une architecture client-serveur ou peer to peer.
 

 ## Broker
 Les Broker servent à établir des connexions entre deux tâches.
 Ils sont identifiés par une chaîne de caractères et il n'en existe qu'un par JVM.
 
 ### Channel accept(int port)
 Méthode bloquante qui attend de recevoir des demandes de connexion sur le port passé en paramètre, et renvoie le canal dédié
 
 ### Channel connect(String name, int port)
 Méthode bloquante qui établit une demande de connexion au Broker représenté par le nom passé en paramètre, sur le port de destination demandé
 
 Le port est un entier quelconque mais doit être identique entre `connect` et `accept` afin d'établir la connexion.
 Ces deux méthodes renvoient le canal de communication, utilisable par les tâches.
 
 
## Channel
Les Channels sont des canaux de communication bidirectionnels en flux d'octets (FIFO et Loss Less) entre deux points (tâches).
Ils sont transmis aux tâches après l'établissement de la connexion par les brokers.

### int read(byte[] bytes, int offset, int length)
Méthode non bloquante qui permet de recevoir des octets dans le tableau passé en paramètre. 
Cherche à lire `length` octets à partir de la position `offset`.
Renvoie le nombre d'octets effectivement lus.

### int write(byte[] bytes, int offset, int length)
Méthode non bloquante qui permet d'écrire des octets dans le tableau passé en paramètre. 
Cherche à écrire `length` octets à partir de la position `offset`.
Renvoie le nombre d'octets effectivement écrits.

### void disconnect()
Coupe une connexion entre deux tâches.
Si la rupture de connexion intervient ??????

### boolean disconnected()
Teste si le canal de communication est déconnecté (à appeler avant de lire ou écrire des données).
Renvoie True si le canal est déconnecté, False si il est toujours actif.





