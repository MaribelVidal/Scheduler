package persistence.DAO;

import business.*;

import java.sql.Connection;

import java.util.HashMap;

import java.util.Map;

public class DAOFactory {

    private static Map<Class<?>, DAO<?>> daoInstances = new HashMap<>();

    public static <T> DAO<T> getDAO(Class<T> clazz, Connection conn) throws Exception {

        if (!daoInstances.containsKey(clazz)) {

            if (clazz == Teacher.class) {

                daoInstances.put(clazz, new TeacherDAO(conn));

            } else if (clazz == Subject.class) {

                daoInstances.put(clazz, new SubjectDAO(conn));

            } else if (clazz == Classroom.class) {

                daoInstances.put(clazz, new ClassroomDAO(conn));

            } else if (clazz == StudentGroup.class) {

                daoInstances.put(clazz, new StudentGroupDAO(conn));

            } else if (clazz == TimePeriod.class) {

                daoInstances.put(clazz, new TimePeriodDAO(conn));

            } else if (clazz == StudentGroup.class) {

                daoInstances.put(clazz, new StudentGroupDAO(conn));

            }  else {

                throw new Exception("DAO not found for class: " + clazz.getSimpleName());

            }

        }

        return (DAO<T>) daoInstances.get(clazz);

    }

}
