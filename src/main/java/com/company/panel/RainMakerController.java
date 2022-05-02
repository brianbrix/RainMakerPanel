package com.company.panel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FilenameUtils;

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
    protected void onBuildButtonClicked() throws IOException, InterruptedException, URISyntaxException {
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
        outputWriter2 = new BufferedWriter(new FileWriter(file2));
        outputWriter2.append("\n").append(LocalDateTime.now().toString());
        for (String key : keys) {
            outputWriter.write(key);
            outputWriter.newLine();
            outputWriter2.append("\n").append(key);
        }
        outputWriter.flush();
        outputWriter.close();
        outputWriter2.append("\n").append("###########");
        outputWriter2.flush();
        outputWriter2.close();
        System.out.println("Keys must not be less tha 3!!!");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("File Writing");
        alert.setHeaderText("Results:");
        alert.setContentText("Written to file... Now we build the app.");
        Optional<ButtonType> res = alert.showAndWait();
        if(res.isPresent()) {
            if(res.get().equals(ButtonType.OK))
               runMini();
        }
        lastBuild.setText(LocalDateTime.now().toString());
        String content = readFile(fileName2, StandardCharsets.UTF_8);
        String[] contentArray = content.split("###########");
        int count = contentArray.length;
        System.out.println(count);
        counter.setText(String.valueOf(count));


    }
    private static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    @FXML
    protected void showHistory() throws URISyntaxException {
        System.out.println("Histo");
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
}