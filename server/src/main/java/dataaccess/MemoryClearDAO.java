package dataaccess;

public class MemoryClearDAO {
    private UserDAO userDAO;
    private  AuthDAO authDAO;
    private GameDAO gameDAO;

    public MemoryClearDAO(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO){
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void clear() throws DataAccessException{
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}
