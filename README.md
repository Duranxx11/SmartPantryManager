Smart Pantry Manager
1. Project Overview

Smart Pantry Manager is an Android mobile application developed using Java in Android Studio. The application helps users manage food ingredients stored in their pantry, keep track of quantities and expiry dates, and find recipes they can prepare using the ingredients they already have.

The application uses SQLite to store pantry items and recipe information locally on the device. It includes pantry management, ingredient validation, recipe suggestions, recipe details, settings, and expiry notifications.

The main purpose of the application is to help users organise their food, reduce food waste, and make better use of available ingredients.

2. Application Features
   2.1 Pantry Management

The pantry management feature allows users to add, view, edit, and delete ingredients. Users can enter the ingredient name, quantity, measurement unit, and expiry date where applicable.

The application validates ingredient information before saving it to help prevent invalid entries. Pantry information is stored in the SQLite database, allowing saved items to remain available when the application is closed and reopened.

2.2 Suggested Recipes

The Suggested Recipes feature checks the ingredients available in the pantry against the ingredients required by each recipe.

The application uses strict recipe matching. A recipe is suggested only when every required ingredient is available in a sufficient quantity and the measurement units are compatible.

For example, if a recipe requires two eggs but the pantry contains only one egg, the recipe should not appear in the suggestions.

Ingredient names are normalised to improve matching between common variations in how ingredients are written. If no recipes match the available ingredients, the application displays feedback to the user.

2.3 Recipe Collection and Details

The application contains 18 predefined recipes stored in RecipeSeeder.java.

The recipes included are:

Scrambled Eggs
Cheese Omelette
French Toast
Pancakes
Grilled Cheese Sandwich
Tomato Pasta
Garlic Butter Pasta
Spaghetti Bolognese
Tuna Pasta Salad
Egg Fried Rice
Vegetable Stir Fry
Chicken Curry
Chicken and Rice
Tomato Soup
Mashed Potatoes
Potato Wedges
Banana Smoothie
Oat Porridge

Users can open a recipe to view its ingredients and preparation instructions.

The recipes are inserted into the database during initial database creation. If additional recipes are added after the database has already been created, an appropriate reseeding or database migration process may be required for existing installations.

2.4 Settings and Expiry Notifications

The application includes a Settings screen and expiry notification functionality.

Expiry notifications are intended to help users keep track of ingredients approaching their expiry dates. The application uses Android background work functionality to schedule expiry-related checks.

Notifications may depend on Android notification permissions, the Android version, and the device's background execution settings.

3. Technologies Used

The project uses the following technologies:

Java: Used to implement the application logic.
Android Studio: Used to develop, build, and test the application.
XML: Used to design the application layouts and define resources.
SQLite: Used to store pantry items and recipes locally.
SQLiteOpenHelper: Used to create and manage the database.
RecyclerView and Adapter: Used to display pantry items in a list.
Intents: Used to navigate between activities and pass information where required.
Android WorkManager: Used to schedule background expiry-related tasks.
Git and GitHub: Used for source-code version control and project hosting.
4. Database Choice and Justification

SQLite was selected as the database for Smart Pantry Manager because it is supported by Android and allows information to be stored locally on the device. It does not require a separate database server or a constant internet connection.

SQLite is suitable for this application because it supports creating, reading, updating, and deleting records. It also allows related information to be organised into separate tables.

The database makes it possible to save pantry items and recipe information so that users can access the data again when they reopen the application. It also supports the strict recipe-matching feature by allowing the application to compare the ingredients available in the pantry with those required by each recipe.

The database is managed by DatabaseHelper.java. The database filename is smart_pantry.db, and its current version is set to 2.

5. Database Design

The application uses three main database tables: pantry_items, recipes, and recipe_ingredients.

5.1 Pantry Items Table

The pantry_items table stores the ingredients added by the user.

It contains the following fields:

id: A unique identifier for each pantry item, generated automatically.
name: The name of the ingredient.
normalized_name: A standardised version of the ingredient name used for matching.
quantity: The amount of the ingredient available.
unit: The measurement unit, such as grams, kilograms, or pieces.
expiry_date: An optional expiry date stored as a timestamp.

This table allows the application to keep track of the ingredients available in the user's pantry.

5.2 Recipes Table

The recipes table stores the main information for each recipe.

It contains these fields:

id: A unique identifier for each recipe.
name: The name of the recipe.
steps: The preparation instructions for the recipe.

Each recipe has its own identifier, name, and instructions.

5.3 Recipe Ingredients Table

The recipe_ingredients table stores the ingredients required for each recipe.

It contains these fields:

id: A unique identifier for each recipe ingredient record.
recipe_id: A foreign key linking the ingredient to its recipe.
name: The required ingredient name.
normalized_name: The standardised ingredient name used during matching.
quantity: The quantity required by the recipe.
unit: The measurement unit required by the recipe.

The recipe_id field connects each ingredient to the correct recipe. The database uses a foreign-key relationship with cascading deletion so that the associated ingredient records can be deleted when their recipe is deleted.

5.4 Relationships Between Tables

The recipes table has a one-to-many relationship with the recipe_ingredients table. This means that one recipe can contain several ingredients, while each ingredient record belongs to a particular recipe.

The pantry_items table stores the ingredients available to the user. The application compares these ingredients with the requirements in recipe_ingredients to determine which recipes can be prepared.

DatabaseHelper.java creates and manages the tables, while RecipeSeeder.java provides the initial collection of recipes.

6. Strict Recipe-Matching Logic

Strict recipe matching is one of the main features of Smart Pantry Manager. It ensures that a recipe is suggested only when all its required ingredients are available in sufficient quantities.

The application first retrieves the ingredients required by a recipe. It then normalises ingredient names and checks whether each required ingredient has a valid name and a positive quantity.

Next, the application searches the pantry for matching ingredients and calculates the available quantity using compatible measurement units. If the available quantity is less than the required quantity, the recipe is rejected.

The application supports compatible mass units, such as grams and kilograms, and compatible volume units, such as millilitres and litres. It also supports certain count-based units, including pieces and units. Compatible measurements are converted to a common base unit before comparison. For example, one kilogram is equivalent to 1,000 grams.

Other units are compared only when their normalised unit names match. Incompatible measurement units are not treated as equivalent.

A recipe is suggested only when every required ingredient passes these checks. If an ingredient is missing or the available quantity is insufficient, the recipe is excluded from the suggestions.

This approach helps ensure that the application recommends recipes the user can actually prepare with the ingredients currently available.

7. Project Structure

The following structure shows the main files and folders used in the application.

text
SmartPantryManager/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/durangoindsamy/smartpantrymanager/
│           │       ├── adapter/
│           │       │   └── PantryAdapter.java
│           │       ├── data/
│           │       │   ├── DatabaseHelper.java
│           │       │   └── RecipeSeeder.java
│           │       ├── model/
│           │       │   ├── PantryItem.java
│           │       │   ├── Recipe.java
│           │       │   └── RecipeIngredient.java
│           │       ├── util/
│           │       │   └── IngredientNormalizer.java
│           │       ├── AddEditPantryActivity.java
│           │       ├── ExpiryNotificationScheduler.java
│           │       ├── PantryExpiryWorker.java
│           │       ├── PantryListActivity.java
│           │       ├── RecipeDetailActivity.java
│           │       ├── SettingsActivity.java
│           │       └── SuggestedRecipesActivity.java
│           ├── res/
│           │   ├── layout/
│           │   │   ├── activity_add_edit_pantry.xml
│           │   │   ├── activity_pantry_list.xml
│           │   │   ├── activity_recipe_detail.xml
│           │   │   ├── activity_settings.xml
│           │   │   ├── activity_suggested_recipes.xml
│           │   │   ├── item_pantry.xml
│           │   │   └── item_suggested_recipe.xml
│           │   ├── menu/
│           │   │   ├── menu_bottom_nav.xml
│           │   │   └── menu_pantry.xml
│           │   ├── values/
│           │   │   ├── colors.xml
│           │   │   ├── strings.xml
│           │   │   └── themes.xml
│           │   └── mipmap-*/, drawable/, xml/ (launcher icons and backup rules)
│           └── AndroidManifest.xml
├── README.md
└── build.gradle.kts, settings.gradle.kts, gradle/ (Gradle build files)
Important Files
PantryListActivity.java displays and manages the pantry list.
AddEditPantryActivity.java handles adding and editing pantry ingredients.
DatabaseHelper.java creates the database and manages database operations.
RecipeSeeder.java defines the initial collection of 18 recipes.
PantryAdapter.java connects pantry data to the list interface.
SuggestedRecipesActivity.java displays recipes that match the pantry ingredients.
RecipeDetailActivity.java displays the details of a selected recipe.
SettingsActivity.java lets the user turn expiry alerts on or off (layout: activity_settings.xml).
menu_bottom_nav.xml defines the bottom navigation bar (Pantry and Recipes) shown on the Pantry List and Suggested Recipes screens.
menu_pantry.xml defines the Settings icon in the top-right corner of the Pantry List toolbar.
IngredientNormalizer.java helps standardise ingredient names.
ExpiryNotificationScheduler.java manages the scheduling of expiry-related background tasks.
PantryExpiryWorker.java performs scheduled expiry-related work.
AndroidManifest.xml declares application components and required Android configuration.
colors.xml defines application colours.
strings.xml stores reusable text resources.
themes.xml defines application theme resources.
8. Setup and Run Instructions
   8.1 Requirements

To run the application, the following software and tools are required:

Android Studio.
A compatible Java Development Kit (JDK) for the project's Gradle configuration.
Android SDK and the required Android SDK platform.
An Android emulator or physical Android device.
Git, if the project is being downloaded from GitHub.
8.2 Clone the Repository

Open a terminal and run the following command:

bash
git clone https://github.com/Duranxx11/SmartPantryManager.git

Then open the project directory:

bash
cd SmartPantryManager
8.3 Open the Project in Android Studio
Open Android Studio.
Select Open.
Choose the SmartPantryManager project folder.
Allow Gradle synchronisation to complete.
Install any required SDK components if Android Studio requests them.
8.4 Run the Application
Start an Android emulator or connect a physical Android device.
Select the app run configuration in Android Studio.
Click Run.
Wait for the application to install and launch.
Test the pantry management, recipe suggestions, recipe details, settings, and expiry notifications.

The application uses a local SQLite database, so its main database operations do not require a remote server.

9. Testing

The following tests should be performed to check that the application works as expected.

9.1 Pantry Management

Add a valid ingredient and check whether it appears in the pantry list. Edit the ingredient and confirm that the updated information is displayed. Delete the ingredient and verify that it is removed from the list.

9.2 Input Validation

Try saving an ingredient with missing information, an invalid quantity, or a non-positive quantity. The application should display an appropriate error message and prevent invalid information from being saved.

9.3 Data Persistence

Add an ingredient, close the application, and open it again. The saved ingredient should still be available because the information is stored in SQLite.

9.4 Strict Recipe Matching

Add all the ingredients required by a recipe in sufficient quantities. Check whether the recipe appears in the suggestions. Then remove one required ingredient or reduce its quantity below the required amount. The recipe should no longer be suggested.

9.5 Measurement Unit Handling

Test compatible units, such as grams and kilograms, to confirm that the application compares quantities correctly after conversion. Also test incompatible units to ensure they are not incorrectly treated as equivalent.

9.6 Empty Recipe Suggestions

Use pantry ingredients that do not satisfy the requirements of any recipe. The application should provide clear feedback when no matching recipes are available.

9.7 Recipe Details

Open a suggested recipe and check that its ingredients and preparation instructions are displayed correctly.

9.8 Settings and Notifications

Open the Settings screen and test its available controls. Test expiry notifications and confirm that they work under the relevant scheduling and permission conditions.

The results of these tests are recorded in the assignment report.

10. Privacy and Data Storage

Smart Pantry Manager stores pantry and recipe information in a local SQLite database on the user's device. The main pantry management and recipe-matching functions do not require a remote database server.

Users should still protect their devices and consider the device's backup and security settings. The application should request only the permissions required by its implemented features.

Expiry notifications may require notification permission on supported Android versions.

11. Limitations and Future Improvements

There are several ways the application could be improved in future versions.

11.1 Improved Unit Support

Additional measurement units and more advanced conversions could be introduced. Ingredient-specific conversions could also be considered when weight and volume measurements cannot be directly compared.

11.2 More Recipes

The recipe collection could be expanded to provide users with more meal options. Future versions could also include categories, dietary preferences, cooking times, and difficulty levels.

11.3 Improved Expiry Management

Expiry management could be improved through configurable reminder periods, clearer expiry status indicators, and more flexible notification settings.

11.4 Database Improvements

Future database upgrades could preserve existing user data and introduce new recipes through database migrations or a dedicated reseeding process rather than requiring a database reset.

11.5 User Interface Improvements

The application could be improved further through accessibility features, additional filtering options, clearer feedback messages, and usability testing.

12. Conclusion

Smart Pantry Manager provides a way for users to organise pantry ingredients and identify recipes that can be prepared with the food they have available.

The application combines Java-based Android development, XML layouts, SQLite database management, CRUD operations, input validation, data persistence, and strict recipe-matching logic.

Developing the application demonstrates how mobile technology can be used to address an everyday problem. It also highlights the importance of accurate database operations, appropriate input validation, and reliable matching logic when building a practical mobile application.

13. References

Android Developers (n.d.) Android Developers Documentation. Available at: https://developer.android.com/ (Accessed: 20 October 2026).

Git (n.d.) Git Documentation. Available at: https://git-scm.com/doc (Accessed: 20 October 2026).

GitHub (n.d.) GitHub Documentation. Available at: https://docs.github.com/ (Accessed: 20 October 2026).

SQLite (n.d.) SQLite Documentation. Available at: https://www.sqlite.org/docs.html (Accessed: 20 October 2026).