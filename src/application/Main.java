package application;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;

import java.io.FileReader;

public class Main extends Application {

	private final Map<String, User> userCredentials = new HashMap<>();
    private final String STATIC_MFA_CODE = "123456"; // default mfa code
    private BorderPane mainLayout;
    private TabPane tabPane;
    private CheckBox readPermissions;
    private CheckBox writePermissions;
    private Label messageLabel;
    
    private File uploadedFile; // To keep track of the uploaded file
    private TextField uploadedFileNameField; // To display the name of the uploaded file
    
    
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        mainLayout = new BorderPane();
        setupTabPane();

        Scene scene = new Scene(mainLayout, 400, 400);
        primaryStage.setTitle("Effort Logger Interface");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupTabPane() {
        tabPane = new TabPane();
        Tab homeTab = new Tab("Home");
        homeTab.setContent(createLoginUI());
        tabPane.getTabs().add(homeTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mainLayout.setCenter(tabPane);
    }

    private VBox createLoginUI() {
        VBox loginLayout = new VBox(10);
        setupLoginLayout(loginLayout);

        GridPane grid = setupLoginGrid();
        TextField userTextField = new TextField();
        PasswordField pwBox = new PasswordField();
        TextField mfaField = new TextField();  // Adding MFA field
        Label errorMsg = setupLoginFieldsAndActions(grid, userTextField, pwBox, mfaField);

        Button signUpBtn = setupSignUpButton();

        Button loginButton = new Button("Log In");
        loginButton.setOnAction(event -> loginUser(userTextField, pwBox, mfaField, errorMsg));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, signUpBtn);

        loginLayout.getChildren().addAll(
                new Label("EffortLogger Login"),
                new Label("Please sign in with your credentials"),
                grid, buttonBox, errorMsg
        );

        return loginLayout;
    }

    
    private void setupLoginLayout(VBox loginLayout) {
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(10, 10, 10, 10));
    }

    private GridPane setupLoginGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        return grid;
    }

    private Label setupLoginFieldsAndActions(GridPane grid, TextField userTextField, PasswordField pwBox, TextField mfaField) {
        grid.addRow(0, new Label("Username:"), userTextField);
        grid.addRow(1, new Label("Password:"), pwBox);
        grid.addRow(2, new Label("Enter MFA Code:"), mfaField);  // MFA input row

        Label errorMsg = new Label();
        errorMsg.setStyle("-fx-text-fill: red;");

        return errorMsg;
    }

    private Button setupSignUpButton() {
        Button signUpBtn = new Button("Sign Up");
        signUpBtn.setOnAction(event -> openSignUpTab());
        return signUpBtn;
    }

     // Static MFA Code

    private void loginUser(TextField userTextField, PasswordField pwBox, TextField mfaField, Label errorMsg) {
        String username = userTextField.getText().trim();
        String password = pwBox.getText().trim();
        String enteredMFA = mfaField.getText().trim();

        if (userCredentials.containsKey(username)) {
            User user = userCredentials.get(username);
            if (user.getPassword().equals(password)) {
                if (STATIC_MFA_CODE.equals(enteredMFA)) {
                    displayLoggedInUI(user.getFirstName(), user.getLastName(), user.getRole());
                } else {
                    errorMsg.setText("Invalid MFA code. Try again.");
                }
            } else {
                errorMsg.setText("Invalid password. Try again.");
            }
        } else {
            errorMsg.setText("Username not found. Try again or sign up.");
        }
    }


    private VBox createSignUpUI() {
        VBox signUpLayout = new VBox(10);
        signUpLayout.setAlignment(Pos.CENTER);
        signUpLayout.setPadding(new Insets(10, 10, 10, 10));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        TextField firstNameTextField = new TextField();
        TextField lastNameTextField = new TextField();
        TextField userNameTextField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        ChoiceBox<String> roleChoice = new ChoiceBox<>();
        roleChoice.getItems().addAll("Admin", "User");
        roleChoice.getSelectionModel().selectFirst();

        grid.addRow(0, new Label("First Name:"), firstNameTextField);
        grid.addRow(1, new Label("Last Name:"), lastNameTextField);
        grid.addRow(2, new Label("Username:"), userNameTextField);
        grid.addRow(3, new Label("Password:"), passwordField);
        grid.addRow(4, new Label("Confirm Password:"), confirmPasswordField);
        grid.addRow(5, new Label("Role:"), roleChoice);

        Button signUpButton = new Button("Sign Up");
        signUpButton.setOnAction(event -> signUpUser(firstNameTextField, lastNameTextField, userNameTextField, passwordField, confirmPasswordField, roleChoice));

        signUpLayout.getChildren().addAll(
                new Label("EffortLogger Sign Up"),
                new Label("Please sign up with your credentials"),
                grid, signUpButton
        );

        return signUpLayout;
    }
    
    public class User {
        private String firstName;
        private String lastName;
        private String username;
        private String password;
        private String role;

        public User(String firstName, String lastName, String username, String password, String role) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.password = password;
            this.role = role;
        }

        // Getters
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getRole() { return role; }

    }


    private void signUpUser(TextField firstNameTextField, TextField lastNameTextField, TextField userNameTextField, PasswordField passwordField, PasswordField confirmPasswordField, ChoiceBox<String> roleChoice) {
        String firstName = firstNameTextField.getText().trim();
        String lastName = lastNameTextField.getText().trim();
        String username = userNameTextField.getText().trim();
        String password = passwordField.getText().trim();
        
//        String coverUserPassword = "########";
        String confirmPassword = confirmPasswordField.getText().trim();
        String role = roleChoice.getValue();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "All fields must be filled out.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Passwords do not match.");
            return;
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{7,}$")) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Password does not meet the criteria.");
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Username and Password cannot be empty.");
            return;
        }

        if (userCredentials.containsKey(username)) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Username already exists. Choose a different one.");
        } 
        if (userCredentials.containsKey(username)) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Username already exists. Choose a different one.");
        } else {
        	User newUser = new User(firstName, lastName, username, password, role);
            userCredentials.put(username, newUser);
            saveUserDetailsToFile(username, password, role); // call method to save user details
            showAlert(Alert.AlertType.INFORMATION, "Signup Success", "Account created successfully. You can now log in.");
            tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedItem());  // Close the current SignUp tab
            tabPane.getSelectionModel().selectFirst(); // Go back to the Login tab
        }
    }
    
    private void displayLoggedInUI(String firstName, String lastName, String role) {
        // Home tab layout
    	VBox homeLayout = new VBox(10);
        homeLayout.setAlignment(Pos.CENTER);
        Label welcomeLabel = new Label("Welcome, " + lastName + ", " + firstName + ": " + role);
        homeLayout.getChildren().add(welcomeLabel);

        // Home Tab
        Tab homeTab = new Tab("Home");
        homeTab.setContent(homeLayout);

        // EffortLogger tab layout
        GridPane effortLoggerLayout = new GridPane();
        effortLoggerLayout.setAlignment(Pos.CENTER);
        effortLoggerLayout.setHgap(10);
        effortLoggerLayout.setVgap(10);
        effortLoggerLayout.setPadding(new Insets(10, 10, 10, 10));

        // Add components to the effortLoggerLayout
        addEffortLoggerComponents(effortLoggerLayout);

        // Effort Logger Tab
        Tab effortLoggerTab = new Tab("Effort Logger");
        effortLoggerTab.setContent(new ScrollPane(effortLoggerLayout)); // Wrap in a ScrollPane for larger content
        
        // Clear any existing tabs and add the new tabs
        tabPane.getTabs().clear();
        tabPane.getTabs().addAll(homeTab, effortLoggerTab); // Add both tabs

        // Select the Home tab by default
        tabPane.getSelectionModel().select(homeTab);

        setupLogoutButton();
    }
    
    private void addEffortLoggerComponents(GridPane grid) {
        // TextFields for the name and ID
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField employeeIdField = new TextField();
        ComboBox<String> projectListComboBox = new ComboBox<>();
        projectListComboBox.getItems().addAll("Project 1", "Project 2");
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        TextField daysField = new TextField();
        TextField hoursField = new TextField();
        Button uploadFileButton = new Button("Upload File");
        uploadFileButton.setOnAction(event -> uploadFile(grid)); // Existing upload file method
        Button submitButton = new Button("Submit");
        // submitButton.setOnAction(event -> submitEffort()); // Implement this method for submitting the effort

        // Arrange components in the grid
        grid.add(new Label("First Name"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name"), 2, 0);
        grid.add(lastNameField, 3, 0);
        grid.add(new Label("Employee ID#"), 0, 1);
        grid.add(employeeIdField, 1, 1);
        grid.add(new Label("Project List"), 0, 2);
        grid.add(projectListComboBox, 1, 2);
        grid.add(new Label("Date Started"), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label("Date Ended"), 2, 3);
        grid.add(endDatePicker, 3, 3);
        grid.add(new Label("Days"), 0, 4);
        grid.add(daysField, 1, 4);
        grid.add(new Label("Hours"), 2, 4);
        grid.add(hoursField, 3, 4);
        grid.add(uploadFileButton, 0, 5, 2, 1);
        grid.add(submitButton, 2, 5, 2, 1);
    }
    
    private void saveUserDetailsToFile(String username, String password, String role) {
        String userDetail = username + " " + password + " " + role + System.lineSeparator();

        File file = new File("userDetails.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(userDetail);
            writer.flush(); // Make sure to flush the stream.
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
            return;
        }

        // Read the file contents back to ensure the data was written.
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("File contains: " + line);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading from the file.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openSignUpTab() {
        Tab signUpTab = new Tab("Sign Up");
        signUpTab.setContent(createSignUpUI());
        tabPane.getTabs().add(signUpTab);
        tabPane.getSelectionModel().select(signUpTab);
    }

    private void setupLogoutButton() {
        Button logoutButton = new Button("Log Out");
        logoutButton.setOnAction(event -> {
            tabPane.getTabs().clear();
            Tab homeTab = new Tab("Home");
            homeTab.setContent(createLoginUI());
            tabPane.getTabs().add(homeTab);
        });
        mainLayout.setTop(logoutButton);
    }

    private void uploadFile(GridPane effortLoggerLayout) {
    	 FileChooser fileChooser = new FileChooser();
    	    fileChooser.setTitle("Open Resource File");
    	    // Set extension filter for PDF files
    	    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
    	    fileChooser.getExtensionFilters().add(extFilter);

    	    File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Create a new row in the GridPane for the uploaded file
            int nextRow = getNextEmptyRow(effortLoggerLayout); // This method needs to find the next empty row in the GridPane

            Label fileNameLabel = new Label(selectedFile.getName());
            Button cpButton = new Button("Change Permissions ");
            
            cpButton.setOnAction(event -> displayFilePermissionsUI(mainLayout));


            // Add the new row with the file name label and the CP button
            effortLoggerLayout.add(fileNameLabel, 0, nextRow);
            effortLoggerLayout.add(cpButton, 1, nextRow);

            System.out.println("File uploaded: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("File selection cancelled.");
        }
    }
    
    private void displayFilePermissionsUI(BorderPane layout) {
        // Clear the previous UI components
        layout.setTop(null);
        layout.setBottom(null);
        layout.setLeft(null);
        layout.setRight(null);
        layout.setCenter(null);

        readPermissions = new CheckBox("Read Permissions");
        writePermissions = new CheckBox("Write Permissions");

        Button setPermissionsButton = new Button("Set the Permissions");
        setPermissionsButton.setOnAction(e -> setFilePermissions());

        Button auditButton = new Button("Audit File Permissions");
        auditButton.setOnAction(e -> auditPermissions());

        messageLabel = new Label("");

        Button goBackBtn = new Button("Go Back");
        goBackBtn.setOnAction(event -> {
            layout.setCenter(createLoginUI());
        });

        // Create a layout for the file permissions UI components
        VBox permissionsLayout = new VBox(10, readPermissions, writePermissions, setPermissionsButton, auditButton, messageLabel, goBackBtn);
        permissionsLayout.setAlignment(Pos.CENTER);
        permissionsLayout.setPadding(new Insets(10));

        // Set the file permissions UI to the center of the BorderPane
        layout.setCenter(permissionsLayout);
    }
    // New method to set file permissions
    private void setFilePermissions() {
        Set<PosixFilePermission> permissions = new HashSet<>();
        if (readPermissions.isSelected()) permissions.add(PosixFilePermission.OWNER_READ);
        if (writePermissions.isSelected()) permissions.add(PosixFilePermission.OWNER_WRITE);
        // if (executePermissions.isSelected()) permissions.add(PosixFilePermission.OWNER_EXECUTE);

        setMessage("Permissions set to: " + permissions);
    }

    // New method to audit file permissions
    private void auditPermissions() {
        if (writePermissions.isSelected()) {
            setMessage("Security risk! Write permissions are enabled.");
        } else {
            setMessage("No security risks detected.");
        }
    }
    private void setMessage(String message) {
        messageLabel.setText(message);
    }
    // Utility method to find the next empty row in the GridPane
    private int getNextEmptyRow(GridPane grid) {
        int rowNum = 0;
        for (Node node : grid.getChildren()) {
            if (GridPane.getRowIndex(node) != null) {
                rowNum = Math.max(rowNum, GridPane.getRowIndex(node) + 1);
            }
        }
        return rowNum;
    }  
}