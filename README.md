# Mongo Plugin for IntelliJ IDEA

## Important Note for the 0.3.0 version

This release supports more than one mongo server, so you will need to reconfigure it after installing it.

## ChangeLog
See CHANGELOG.txt

## Description
This plugin integrates MongoDB Servers with database/collections tree,  Query Runner and Shell console.

## Plugin Compatibility
This plugin was built with JDK 1.6 and ideaIU-11.1.4 version.

## How to install it?
Download this plugin from your IDE (Mongo Plugin)

## Configuration steps

When you open the Mongo explorer, you will see an empty tree :

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-explorerWithoutDB.png?raw=true)

* To manager your Mongo servers, click on the Mongo Settings button located on the upper toolbar of the Mongo explorer Right Panel

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-configuration.png?raw=true)

* You can edit your mongo shell path with a test button
* To add a server, click on the **[+]** button and a dialog will appear
* If the server requires authentication, you can put your credentials
* You can let the plugin connect to the server on the IDE startup by clicking on the corresponding checkbox
* If you want to hide some collections, you can put them in the **Collections to ignore** field.
* You can click on the **Test Connection** button to check your server configuration

## Usage

### Mongo Explorer

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-explorerWithDB.png?raw=true)

The tree displays for each server all databases with its collections. Just double-click on a collection and the results will be displayed in the Mongo Runner panel.
If you double-click on another collection, a new tab will appears side of the previous.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-multipleTab.png?raw=true)


### Mongo Runner
The panel is divided into 2 parts.

* Right part displays the results of the query in a tree (max. 300 records).

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-findAll.png?raw=true)

You can copy the result and paste in a text editor.

If you use the Darcula Theme then the results are displayed as follow:

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea_darcula.png?raw=true)

* Left part allows to specify a query (in json format).

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-findWithFilter.png?raw=true)

When you type **CTRL+SPACE** key shortcut a popup is displayed in which you can select query operator.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-operatorPopup.png?raw=true)

Either you click on the run query button or else type **CTRL+F5** shortcut to run the query
If your server version is at least 2.2, you can use the aggregation framework. You can add a pipeline operation by clicking on the [+] button.
You can also copy/paste the query you wrote.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-aggregation.png?raw=true)

If you have an error during query execution, a feedback panel is displayed below:

* In case of bad JSON syntax

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-errorInExecution.png?raw=true)

* In case of general error

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-errorInExecutionCommand.png?raw=true)

**Note**: If you use Ultimate Edition, JSON syntax highlighting is enabled.

### Mongo shell integration

If you set the mongo client path (e.g. /usr/bin/mongo), you can run the console by clicking the menu item **Tools -> Mongo Shell**.

![Browser](https://github.com/dboissier/mongo4idea/blob/master/doc/mongo4idea-shell.png?raw=true)


## Thanks
I would like to thank:
* MongoDB team
* Mongo Java Driver team
* Jetbrains Team for the great sources of the Groovy Plugin that help me to improve this stuff
* Mark James author of the famfamfam web site who provides beautiful icons.
* [Jean Baptiste Potonnier](https://github.com/JJeeb) for the suggestion to create this plugin
* Neko team, my first beta testers ;)
* All users of this plugin who sent me valuable suggestions.
* My wife and my daughter who support me to have fun in software development and also remind me my husband/father duty ;).
