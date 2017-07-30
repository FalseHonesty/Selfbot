package jselfbot.commands;

import jselfbot.Command;
import jselfbot.utils.WrapUtil;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class GithubCmd extends Command {

    private GitHub githubApi;

    public GithubCmd() {
        this.name = "github";
        this.description = "returns information from the Github API";
        this.arguments = "<action> <subaction> <args>";
        this.type = Type.DELETE_AND_RESEND;

        try {
            this.githubApi = GitHub.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void execute(String args, MessageReceivedEvent event) {
        if (args == null || args.isEmpty()) {
            reply("Possible Actions:\n"
                            + "`repo`\n"
                            + "`gist`"
                    , event
            );

            return;
        }

        ArrayList<String> actionArgsArr = new ArrayList<>(Arrays.asList(args.split(" ")));
        String action = actionArgsArr.remove(0);
        String subAction = actionArgsArr.size() > 0 ? actionArgsArr.remove(0) : "";
        String actionArgs = StringUtils.join(actionArgsArr, ' ');
        actionArgs = actionArgs == null ? "" : actionArgs;
        ArrayList<String> subActionAgsArr = new ArrayList<>(Arrays.asList(actionArgs.split(" ")));


        switch (action.toLowerCase()) {
            case "repo":
                GHRepository repo;

                try {
                    repo = actionArgs.equals("") ? null : githubApi.getRepository(actionArgs);
                } catch (IOException e) {
                    tempReply(":x: Not a repo! :x:", event);
                    return;
                }

                switch (subAction.toLowerCase()) {
                    case "readme":
                        GHContent readme;
                        ArrayList<String> readmeSplit;

                        try {
                            readme = repo.getReadme();
                            readmeSplit = WrapUtil.splitStringByLength(IOUtils.toString(readme.read()), 1950);
                        } catch (IOException e) {
                            tempReply(":x: Repo doesn't have a README! :x:", event);
                            return;
                        }

                        for (String message : readmeSplit) {
                            reply("```md\n" + message.replace("```", "") + "```", event);
                        }

                        return;
                    case "desc":
                        reply("```md\n" + repo.getDescription() + "```", event);

                        return;
                    case "collabs":
                        try {
                            reply("```md\n" + repo.getCollaboratorNames().toString() + "```", event);
                        } catch (IOException e) {
                            tempReply(":x: Repo doesn't have collaborators! :x:", event);
                        }

                        return;

                    case "issues":
                        StringBuilder toReply = new StringBuilder("[Count - ").append(repo.getOpenIssueCount());

                        for (GHIssue issue : repo.listIssues(GHIssueState.OPEN)) {
                            toReply.append(", ").append(issue.getHtmlUrl());
                        }

                        toReply.append("]");

                        reply(toReply.toString(), event);

                        return;

                    default:
                        reply("Possible Actions:\n"
                                        + "`readme`\n"
                                        + "`desc`\n"
                                        + "`collabs`\n"
                                        + "`issues`"
                                , event
                        );

                        return;
                }

            case "gist":
                switch (subAction.toLowerCase()) {
                    case "create":

                        if (actionArgs.equals("")) {
                            reply("Args: `filename` `public` `messageId` `description`", event);
                            return;
                        }

                        GHGistBuilder ghGistBuilder = githubApi.createGist();

                        String filename = subActionAgsArr.remove(0);
                        boolean publicity = Boolean.parseBoolean(subActionAgsArr.remove(0));
                        String messageId = subActionAgsArr.remove(0);
                        String description = StringUtils.join(subActionAgsArr, ' ');
                        final String[] gistContent = {null};

                        event.getTextChannel().getHistoryAround(messageId, 2).queue(mh -> {
                            if (mh.getRetrievedHistory().isEmpty()) {
                                tempReply("No message history around `" + messageId + "`", event);
                                return;
                            }

                            Message msg = mh.getRetrievedHistory().size() == 1 || mh.getRetrievedHistory().size() == 2 ? mh.getRetrievedHistory().get(0) : mh.getRetrievedHistory().get(1);
                            gistContent[0] = msg.getStrippedContent();
                            msg.getChannel().sendMessage(gistContent[0]);

                            ghGistBuilder.public_(publicity);
                            ghGistBuilder.description(description);
                            ghGistBuilder.file(filename, gistContent[0]);

                            try {
                                GHGist gist = ghGistBuilder.create();
                                reply(gist.getHtmlUrl().toString(), event);
                            } catch (IOException e) {
                                e.printStackTrace();
                                tempReply(":x: Can't make gist! :x:", event);
                            }
                        });

                        return;

                    case "get":
                        try {
                            GHGist gist = githubApi.getGist(subActionAgsArr.get(0));

                            if (subActionAgsArr.size() > 1) {
                                GHGistFile gistFile = gist.getFile(subActionAgsArr.get(1));

                                String starter = "```\n";

                                for (String message : WrapUtil.splitStringByLength(gistFile.getContent(), 1950)) {
                                    reply(starter + message + "\n```", event);
                                }
                            } else {
                                Set<String> fileNames = gist.getFiles().keySet();
                                reply(fileNames.toString(), event);
                            }

                        } catch (IOException e) {
                            tempReply(":x: Can't get gist! :x:", event);
                        }
                        return;

                    default:
                        reply("Possible Actions:\n"
                                        + "`create`\n"
                                        + "`get`"
                                , event
                        );

                        return;
                }

            case "user":

                if (subAction.equals("")) {
                    reply("Possible Action:\n"
                                    + "`getsrepos`\n"
                                    + "`getarepos`",
                            event);
                    return;
                }

                GHUser user;

                try {
                    user = githubApi.getUser(subActionAgsArr.get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                    tempReply(":x: Not a user! :x:", event);
                    return;
                }

                switch (subAction.toLowerCase()) {
                    case "getstarred":
                        StringBuilder toReply = new StringBuilder("[");

                        for (GHRepository repository : user.listStarredRepositories()) {
                            toReply.append(repository.getHtmlUrl()).append(", ");
                        }

                        if (toReply.length() != 1) {
                            toReply.replace(toReply.length() - 2, toReply.length(), "");
                        }

                        toReply.append("]");

                        reply(toReply.toString(), event);

                        break;

                    case "getrepos":
                        toReply = new StringBuilder("[");

                        for (GHRepository repository : user.listRepositories()) {
                            toReply.append(repository.getHtmlUrl()).append(", ");
                        }

                        if (toReply.length() != 1) {
                            toReply.replace(toReply.length() - 2, toReply.length(), "");
                        }

                        toReply.append("]");

                        reply(toReply.toString(), event);

                        break;

                    case "getgists":
                        toReply = new StringBuilder("[");

                        try {
                            for (GHGist repository : user.listGists()) {
                                toReply.append(repository.getHtmlUrl()).append(", ");
                            }
                        } catch (IOException e) {
                            tempReply(":x: Can't get gists! :x:", event);
                            e.printStackTrace();
                            return;
                        }

                        if (toReply.length() != 1) {
                            toReply.replace(toReply.length() - 2, toReply.length(), "");
                        }

                        toReply.append("]");

                        reply(toReply.toString(), event);

                        break;
                }

                return;

            default:
                reply("Possible Actions:\n"
                                + "`repo`\n"
                                + "`gist`"
                        , event
                );

                return;
        }
    }
}
