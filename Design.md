# Design de l'envoi de messages synchrones

## QueueBroker

Un `QueueBroker` est une surcouche au-dessus d'un `Broker` qui permet d'envoyer des messages de manière synchrone. Il est défini à l'aide d'un Broker et est identifié par le même nom.
À l'établissement d'une connexion entre deux tâches, des `MessageQueue` sont crées et retournées.

## MessageQueue

Une `MessageQueue` permet le transfert de messages en full-duplex par l'intermédiaire d'un `Channel`.

- void send(byte[] bytes, int offset, int length)

Méthode bloquante (jusqu'à l'envoi du message complet) qui envoie un message de la taille définie. 
La taille est envoyée (sur 4 octets, la taille d'un int) puis le message de la taille définie. Le `MessageQueue` se charge de découper le message en plusieurs envois (via un `Channel`) si nécessaire.
Si la connexion se ferme pendant l'envoi, une exception est levée et le message n'est pas envoyé en entier.

- byte[] receive()

Méthode bloquante qui reçoit un message en entier. La méthode attendra jusqu'à ce qu'un message complet soit reçu. 
La taille du message est d'abord reçue (sur 4 octets, la taille d'un int) puis le message de la taille définie. Le `MessageQueue` se charge de rassembler le message si celui-ci a été découpé en plusieurs parties (pour l'envoi via un `Channel`).
Si la connexion se ferme pendant la réception, une exception est levée et aucun message n'est retourné.

- void close()

Ferme instantanément la connexion localement (la méthode `closed` renverra True) et notifie l'autre côté de la fermeture de connexion.

- boolean closed()

La connexion est fermée (au moins localement). La tâche ne peut plus ni envoyer ni recevoir de messages.
Pendant la phase de semi-fermeture, la tâche distante peut encore recevoir les messages envoyés avant la fermeture, et ses messages en cours d'envoi sont jetés.
