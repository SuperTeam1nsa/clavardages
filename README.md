# Clavardeur

## Installation
### Terminal
Dans le dossier de votre choix, cloner le projet :<br>
```git clone https://github.com/usernamedjpris/clavardage.git```<br>
Compiler le projet :<br>
```ant```<br>
### Serveur de présence


## Manuel d'utilisation
Lancer le projet après une première compilation:<br>
```ant relaunch```<br>
### Fonctionnalités principales
- Choix et gestion d'un nouveau pseudo unique
- Découverte des indoor users
- Envoi/Réception des messages entre utilisateurs
- Gestion de l'historique (avec recherche et affichage) des anciens messages
### Fichier de configuration [`config.ini`](config.ini)
Les données suivantes peuvent y être configurées manuellement :
- port TCP de l'application
- port UDP de l'application
- @IP, port du serveur de présence
- @IP locale si problème de détection auto IP 
- @MAC locale si problème de détection auto MAC (e.g. s'il n'y a pas le droit de lecture sur l'adresse)
### Astuces & Raccourcis
Envoyer un message texte : `SHIFT + ENTER`<br>
Sélectionner un (ou des) fichier(s) à envoyer : `SHIFT + F`<br>
Ouvrir le dossier des fichiers reçus  : `ALT + O`<br>
Changer dossier de téléchargement : `ALT + C`<br>
Changer de pseudo  : `ALT + P`<br>

NB: Les noms des fichiers reçus sont cliquables ! <br>
Et les liens écrits à la volée `https://google.com` aussi !

## Procédure de tests
### Cas d'utilisation #1
## Choix d'implémentation
Générer la javadoc à retrouver dans doc/ : <br>
```ant javadoc```<br>

### Design Pattern utilisés
#### Singleton
- BD
- Reseau
#### Observers/Observable
- Thread Serveur TCP → Serveur TCP → Reseau → Controlleur Application
- Serveur UDP → Reseau → Controlleur Application
#### Serialization 
- Message
- Personne
#### Factory
- Message
