# android-inventory-app

### Briefly summarize the requirements and goals of the app you developed. What user needs was this app designed to address?

The goal of this app was to provide a minimalistic inventory management application. This app was created with a small business in mind to aid in inventory tracking. Additionally, features like the SMS notifications can ensure users are notified to order items when they receive a message.

### What screens and features were necessary to support user needs and produce a user-centered UI for the app? How did your UI designs keep users in mind? Why were your designs successful?

The login, registration, inventory home, settings, and specific item page were necessary to support all of the needs for this user-centered UI. Ultimately, users will spend the majority of the time on the 'Inventory Home' screen to create, read, update, or delete items. Including additional features like the quick delete button from the home screen enables users to quickly remove unneeded items. Moreover, rather than immediately delete items, an alert dialog is presented when a user selects a delete option. This ensures users do not accidentally remove items and the action is actually intended.

### How did you approach the process of coding your app? What techniques or strategies did you use? How could those techniques or strategies be applied in the future?

I approached the process of coding my app by strictly reviewing the requirements. By deeply understanding what was required, I could expand on this as ideas came to mind. This process is and will be applied to future projects by implementing was is first desired, and iterating on the initial design to create further improvements.

### How did you test to ensure your code was functional? Why is this process important, and what did it reveal?

I tested using Android Studio's emulator to ensure the application worked as intended on a device. This highlighted short-comings such as incorrect workflow and processes that caused the app to crash. Multiple testing iterations were required to fully test this version of the application. Ultimately, testing revealed easily identifiable errors and allowed me to improve them prior to user feedback.

### Consider the full app design and development process from initial planning to finalization. Where did you have to innovate to overcome a challenge?

Within the app, the most challenging parts were using the RecyclerView and ensuring SMS notifications were sent correctly. To overcome these challenges, I researched documentation and modified the contents for my particular use case.

### In what specific component of your mobile app were you particularly successful in demonstrating your knowledge, skills, and experience?

In the RecyclerView I feel I was particularly successful as each card had multiple onClick features and navigation aspects. The card click populated a previously created page so a new page was not required when selecting a current item. Additionally, this was created to ensure multiple inventory items had the same functions and ensured a consistent UI experience throughtout the app.
