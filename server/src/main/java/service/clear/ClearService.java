package service.clear;

import dataaccess.DataAccessException;
import dataaccess.MemoryClearDAO;
import service.ServiceException;

public class ClearService {
    private final MemoryClearDAO clearDAO;

    public ClearService(MemoryClearDAO clearDAO){
        this.clearDAO = clearDAO;
    }

    public void clear() throws ServiceException{
        try{
            clearDAO.clear();
        } catch (DataAccessException ex){
            throw new ServiceException(500, "Error: " + ex.getMessage());
        }
    }
}
