package org.codinjutsu.tools.mongo.view.console;

import com.intellij.execution.console.LanguageConsoleViewImpl;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;

public class MongoConsoleView extends LanguageConsoleViewImpl {
    public MongoConsoleView(Project project) {
        super(project, "Mongo Console", StdFileTypes.PLAIN_TEXT.getLanguage());
    }


}
