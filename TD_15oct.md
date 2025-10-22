# TD SAR : 15 oct

## Distributed Mutual Exclusion on Ring Overlay

### Data consistency

- Sans modification (la section critique n'a pas été utilisée ou simplement lue), le token est retransmis directement.
- Après modification d'une donnée, le paire envoie la nouvelle version avant d'envoyer le token (dans l'anneau). Chaque paire reçoit la nouvelle version puis le token (FIFO + Lossless) et les transmet dans le même ordre. Une fois arrivé au modificateur (après un tour dans l'anneau), on revient dans le premier cas où seul le token est envoyé. Un modificateur garde en mémoire le numéro de version après modif, pour vérifier si la version qu'il reçoit correspond à sa modification ou non (si oui, envoie juste le token, si non, propage modif puis token). 

### Panne

Tous les pairs connaissent l'ordre complet de l'anneau -> Si un pair meurt, il est sauté.

Un coordinateur est désigné. 

Si un pair meurt en possession d'un token : 

Au bout d'un moment, le coordinateur va demander pourquoi il n'a pas reçu un token depuis longtemps. Il envoie un message dans l'anneau "Est-ce que quelqu'un a le token ?"


## Fault-tolerant Distributed Mutual Exclusion

Le serveur accorde des autorisations d'utiliser des parties de code (pas d'objets) mutex.

### Panne des clients (d'autres reviennent)

Si un client meurt en possession d'un mutex, le serveur (lorsqu'il l'apprend) le libère et passe à la file d'attente.

Si un client meurt dans une file d'attente, on l'enlève de la file d'attente.

### Panne du serveur (Stateless / Stateful)

- Semi-stateful : à chaque modification de la liste des clients connectés, on sauvegarde la liste des clients (identifiant (de l'IP par exemple) + numéro de port) en mémoire (sauvegarde = partie louche pas trop définie).
À la relance du serveur, il se connecte à tous les anciens clients pour leur demander si ils sont actuellement dans un mutex et/ou dans une file d'attente.
Quand chaque client en vie a répondu, le serveur sait quel mutex est utilisé et comment reconstruire les files d'attentes (sans lien avec le précédent ordre).
Si un client est mort en même temps que le serveur et qu'il : 
    - était en possession d'un mutex, le serveur le libère car aucun client ne dit l'utiliser
    - était en file d'attente, il est oublié (tant mieux)


- Stateless : 
En cas de détection de mort du serveur, les clients font périodiquement des demandes de connexion.
Le serveur attend que les clients se connectent et attend 2-3 périodes pour être sûr d'avoir reçu tous les clients en vie (tant de transit de message à prendre en compte).
Les clients envoient chacun les mutex utilisés et les listes d'attente.
Une fois toutes les réponses reçues, on peut retourner dans un état normal et accepter à nouveau des demandes de mutex.

