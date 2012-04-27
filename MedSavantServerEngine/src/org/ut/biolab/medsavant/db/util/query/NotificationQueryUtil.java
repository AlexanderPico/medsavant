/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.query;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.model.Notification;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.serverapi.NotificationQueryUtilAdapter;

/**
 *
 * @author Andrew
 */
public class NotificationQueryUtil extends MedSavantServerUnicastRemoteObject implements NotificationQueryUtilAdapter {

    private static NotificationQueryUtil instance;

    public static synchronized NotificationQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new NotificationQueryUtil();
        }
        return instance;
    }

    public NotificationQueryUtil() throws RemoteException {super();}

    public List<Notification> getNotifications(String sid, String user) throws SQLException, RemoteException {
        List<Notification> result = new ArrayList<Notification>();

        List<ProjectDetails> unpublishedTables = ProjectQueryUtil.getInstance().getUnpublishedTables(sid);
        for(ProjectDetails table : unpublishedTables){
            result.add(new Notification(Notification.Type.PUBLISH, "The variant table for project " + table.getProjectName() + " and reference " + table.getReferenceName() + " has not yet been published. ", table));
        }

        return result;
    }

}