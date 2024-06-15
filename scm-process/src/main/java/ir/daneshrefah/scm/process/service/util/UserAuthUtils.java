package ir.daneshrefah.scm.process.service.util;

import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;

@Deprecated
public class UserAuthUtils {//Todo move this class to related package
    public static GeneralRealPerson getLoggedInPerson() {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        assert loggedInUser != null;
        return (GeneralRealPerson)loggedInUser.getPerson();
    }

    public static String getLoggedInUserNationalCode() {
        GeneralRealPerson loggedInPerson = getLoggedInPerson();
        return loggedInPerson.getNationalCode();
    }
}
