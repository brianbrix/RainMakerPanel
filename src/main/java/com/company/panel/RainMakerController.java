package com.company.panel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.isNull;

public class RainMakerController {
    @FXML
    protected TextArea keysText;
    @FXML
    protected TextArea history;
    @FXML
    protected Label lastBuild;
    @FXML
    protected Label counter;
    private String fileLocation;
    @FXML
    protected Button downloadBtn;
    @FXML
    protected TextArea console;
    @FXML
    protected TextField gitUser;
    @FXML
    protected TextField gitToken;
    private PrintStream ps ;

    public static class Console extends OutputStream {
        private final TextArea console;

        public Console(TextArea console) {
            this.console = console;
        }

        public void appendText(String valueOf) {
            Platform.runLater(() -> console.appendText(valueOf));
        }

        public void write(int b) {
            appendText(String.valueOf((char)b));
        }
    }
    public void initialize() {
        ps = new PrintStream(new Console(console)) ;
        System.setOut(ps);
        System.setErr(ps);
    }

    @FXML
    protected void onBuildButtonClicked() throws IOException, InterruptedException, URISyntaxException, GitAPIException {
        System.out.println("Build Started...");
        String[] keys  = keysText.getText().split("\n");
        String [] bcStarts = {"1","3", "5","K", "L", "M", "xpub", "xprv", "m", "n", "2", "9", "c","tpub","tprv","bc1","tb1"};
        String [] erC20Starts = {"0x"};
        String [] trC20Starts = {"T"};
        if (!(keys.length>=3))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Writing");
            alert.setHeaderText("Results:");
            alert.setContentText("Keys must not be less than 3 !!!");
            alert.showAndWait();
            return;
        }
        if (!(keys[0].length() >= 33 && keys[0].length()<=42 && Arrays.stream(bcStarts).anyMatch(keys[0]::startsWith)))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Writing");
            alert.setHeaderText("Results:");
            alert.setContentText("First address must be Bitcoin Address!!!");
            alert.showAndWait();
            return;
        }
        if (!(keys[1].length()<=42 && Arrays.stream(erC20Starts).anyMatch(keys[1]::startsWith)))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Writing");
            alert.setHeaderText("Results:");
            alert.setContentText("Second address must be ERC20 Address!!!");
            alert.showAndWait();
            return;
        }

        if (!(keys[2].length()<=35 && Arrays.stream(trC20Starts).anyMatch(keys[2]::startsWith)))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Writing");
            alert.setHeaderText("Results:");
            alert.setContentText("Third address must be TRC20 Address!!!");
            alert.showAndWait();
            return;
        }
        String fileName = "../rainmakerr/src/main/resources/key/key.txt";
        String fileName2 = "../rainmakerkeys/keys.txt";
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            fileName = "rainmakerr/src/main/resources/key/key.txt";
            fileName2 = "rainmakerkeys/keys.txt";
        }

        BufferedWriter outputWriter;
        BufferedWriter outputWriter2;
        File file2 = new File(fileName2);
        file2.getParentFile().mkdirs();
        outputWriter = new BufferedWriter(new FileWriter(fileName));
        outputWriter2 = new BufferedWriter(new FileWriter(fileName2, true));
        outputWriter2.append(System.getProperty("line.separator")).append(LocalDateTime.now().toString());
        for (String key : keys) {
            outputWriter.write(key);
            outputWriter.newLine();
            outputWriter2.append(System.getProperty("line.separator")).append(key);
        }
        outputWriter.flush();
        outputWriter.close();
        outputWriter2.append(System.getProperty("line.separator")).append("###########");
        outputWriter2.flush();
        outputWriter2.close();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("File Writing");
        alert.setHeaderText("Results:");
        alert.setContentText("Written to file... Now we build the app.");
        Optional<ButtonType> res = alert.showAndWait();
        boolean pushed = false;
        if(res.isPresent()) {
            if(res.get().equals(ButtonType.OK))
               pushed = push();
        }
        if (pushed) {
            lastBuild.setText(LocalDateTime.now().toString());
            String content = readFile(fileName2, StandardCharsets.UTF_8);
            String[] contentArray = content.split("###########");
            int count = contentArray.length;
            System.out.println(count);
            counter.setText(String.valueOf(count));
            pushed=false;
        }


    }
    private static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    @FXML
    protected void showHistory() throws URISyntaxException {
        String fileName2 = "../rainmakerkeys/keys.txt";
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            fileName2 = "rainmakerkeys/keys.txt";
        }

        File file = new File(fileName2);
        StringBuilder fieldContent = new StringBuilder("");

        try (Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                fieldContent.append(input.nextLine()).append("\n");
            }
            history.setText(String.valueOf(fieldContent));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }
    @FXML
    protected void openFileLocation() {

        String folder = FilenameUtils.getFullPathNoEndSeparator(new File(fileLocation).getAbsolutePath());
        try {
            Desktop.getDesktop().open(new File(folder));
        }catch (Exception e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Location");
            alert.setHeaderText("Error:");
            alert.setContentText("The file could not be found:. "+ e.getLocalizedMessage());
            alert.showAndWait();
            return;
        }

    }


    private void runMini() throws IOException, InterruptedException {
        System.out.println(System.getProperty("os.name"));
        Process p = null;
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux")) {
            p = Runtime.getRuntime().exec("mvn clean install -f ../rainmakerr/pom.xml");
        }
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            p = Runtime.getRuntime().exec("cmd.exe /c mvn clean install -f rainmakerr/pom.xml");
        }
        if (!isNull(p)) {
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader rdr = new BufferedReader(isr);
            while ((rdr.readLine()) != null) {
                String r = rdr.readLine();
                System.out.println(r);
                if (r!=null) {
                    if (r.contains("Installing")) {
                        fileLocation = r.split(" ")[2];
                        System.out.println("Path: " + fileLocation);
                    }
                }
            }


            isr = new InputStreamReader(p.getErrorStream());
            rdr = new BufferedReader(isr);
            while ((rdr.readLine()) != null) {
                System.out.println(rdr.readLine());
            }
            p.waitFor();
        }
        downloadBtn.setDisable(fileLocation == null);
    }
     boolean push() throws GitAPIException, IOException {
        try {
            Repository existingRepo = new FileRepositoryBuilder()
                    .setGitDir(new File("../rainmakerr/.git"))
                    .build();
            Git git = new Git(existingRepo);

            // add remote repo:
            git.add().addFilepattern("../rainmakerr/").call();
            CommitCommand commitCommand = git.commit().setAll(true).setMessage("New Key changes...");
            RevCommit revCommit = commitCommand.call();
            System.out.println(revCommit.getFullMessage());

            // push to remote:
            PushCommand pushCommand = git.push();
            String user= gitUser.getText();
            String token= gitToken.getText();
            System.out.println(user);
            System.out.println(token);
            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, token));
            // you can add more settings here if needed
            Iterable<PushResult> pushResults = pushCommand.call();
            pushResults.forEach(pushResult -> {
                        System.out.println(pushResult.getRemoteUpdates());
                        System.out.println(pushResult.getMessages());
                    }
            );
            return true;
        }catch (TransportException exception)
        {
            System.out.println(exception.getLocalizedMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Github Error");
            alert.setHeaderText("Github Error:");
            alert.setContentText("Please provide your valid username and token.");
            alert.showAndWait();
            return false;
        }


    }


}