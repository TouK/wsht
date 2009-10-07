package pl.touk.humantask.ws;

public class MockSecurityContext implements SecurityContextInterface {

    public UserDetails getLoggedInUser() {
        return new UserDetails() {
            public String getUsername() {
                return "user1";
            }            
        };
    }

}
