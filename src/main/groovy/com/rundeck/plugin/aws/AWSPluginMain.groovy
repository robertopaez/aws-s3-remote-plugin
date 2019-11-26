package com.rundeck.plugin.aws

import com.rundeck.plugin.aws.commands.s3.S3Command
import org.rundeck.toolbelt.CommandRunFailure
import org.rundeck.toolbelt.SubCommand
import org.rundeck.toolbelt.ToolBelt
import org.rundeck.toolbelt.input.jewelcli.JewelInput

@SubCommand
class AWSPluginMain {

    static final String PLUGIN_NAME = "rundeck-aws-cli"

    public static void main(String[] args) throws IOException, CommandRunFailure {

        boolean success = false;
        try {
            success = ToolBelt.belt(PLUGIN_NAME)
                    .add(
                        new S3Command()
                    )
                    .defaultHelpCommands()
                    .ansiColorOutput(true)
                    .commandInput(new JewelInput())
                    .buckle().runMain(args, false)
        } catch (Exception failure) {
            failure.printStackTrace()
        }
        if (!success) {
            System.exit(2);
        }
    }
}
