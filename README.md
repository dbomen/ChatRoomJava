# CHATROOMJAVA

My first "BIG" personal project. This is a Server-Client chatroom application. It was originally a school project, where we had to make a command-line based chatroom with public and private messages. I have since greatly expanded on it and added:

> **Server Side**
> - Custom database (file-based), which contains:
>     - users (name, password, other logic)
>     - histories (contains all private messages sent, *50 message limit*)
> - Logic to interpret revamped Client JSON requests / other requests and new features and commands
> - Database / file managment logic

> **Client side**
> - Login GUI (Swift), *login / singup*
> - Main GUI (javafx)
>     - actively updating GUI with multithreading and other logic
>     - button and/or command usability
>     - other user-friendly abilities

This project was originally in another repository, but I decided to create separate repositories for "BIGGER PROJECTS". The application is now in a functional and acceptable state. I will slowly add some features from the *Goals* list, but this project is not my main focus anymore, as I will start other ones.

> [!NOTE]
> For details about using the application, refer to the [MANUAL](Manuals/Manual.md)\
> I created a short video showcasing all the features in version 1.0.0: [link](https://www.youtube.com/watch?v=JDjVa-9h8oU)\
> For details about releases, refer to the [RELEASES](Manuals/Releases.md)

### Goals
- **FUNCTIONALITY:** Add settings to the Client side and add logic on the Server side so it saves the settings into the database, making the settings Server based (saved to the account). The Server has to send Response, carrying a JSON which holds the client's settings, to the Client on login and the Client has to interpret that correctly. For the first part of this functionality, only make it so the Client can have its GUI color saved. Will add other things later (ideas: font family, font size, ...)
- **REPO:** Revamp the project into Maven / Gradle, not sure yet