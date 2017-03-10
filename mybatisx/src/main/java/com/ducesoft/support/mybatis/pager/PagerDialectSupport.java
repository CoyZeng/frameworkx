package com.ducesoft.support.mybatis.pager;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author coyzeng@gmail.com
 */
public class PagerDialectSupport {

	private static DB db = null;

	public static DB currentDB() {
		return db;
	}

	public static void init(DatabaseMetaData data) {
		try {
			db = resolve(data);
		} catch (SQLException e) {
			db = DB.mysql;
		}
	}

	public static enum DB {
		mysql, mysql5plus, cubrid, hsql, h2, postgresql94, postgresql92, postgresql9, postgresql82, postgresql81, EnterpriseDB, Derby17, Derby16, Derby15, Derby, Ingres, Ingres10, Ingres9, sqlServer, sqlServer05, sqlServer08, sqlServer12, SybaseASE15, SybaseAnywhere, Informix, DB2400, DB2, Oracle12, Oracle10g, Oracle9i, Oracle8i, HDB, Firebird,
	}

	private static synchronized DB resolve(DatabaseMetaData data) throws SQLException {

		final String databaseName = data.getDatabaseProductName();

		if ("CUBRID".equalsIgnoreCase(databaseName)) {
			return DB.cubrid;
		}

		if ("HSQL Database Engine".equals(databaseName)) {
			return DB.hsql;
		}

		if ("H2".equals(databaseName)) {
			return DB.h2;
		}

		if ("MySQL".equals(databaseName)) {
			final int majorVersion = data.getDatabaseMajorVersion();

			if (majorVersion >= 5) {
				return DB.mysql5plus;
			}
			return DB.mysql;
		}

		if ("PostgreSQL".equals(databaseName)) {
			final int majorVersion = data.getDatabaseMajorVersion();
			final int minorVersion = data.getDatabaseMinorVersion();

			if (majorVersion == 9) {
				if (minorVersion >= 4) {
					return DB.postgresql94;
				} else if (minorVersion >= 2) {
					return DB.postgresql92;
				}
				return DB.postgresql9;
			}

			if (majorVersion == 8 && minorVersion >= 2) {
				return DB.postgresql82;
			}

			return DB.postgresql81;
		}

		if ("EnterpriseDB".equals(databaseName)) {
			return DB.EnterpriseDB;
		}

		if ("Apache Derby".equals(databaseName)) {
			final int majorVersion = data.getDatabaseMajorVersion();
			final int minorVersion = data.getDatabaseMinorVersion();

			if (majorVersion > 10 || (majorVersion == 10 && minorVersion >= 7)) {
				return DB.Derby17;
			} else if (majorVersion == 10 && minorVersion == 6) {
				return DB.Derby16;
			} else if (majorVersion == 10 && minorVersion == 5) {
				return DB.Derby15;
			} else {
				return DB.Derby;
			}
		}

		if ("ingres".equalsIgnoreCase(databaseName)) {
			final int majorVersion = data.getDatabaseMajorVersion();
			final int minorVersion = data.getDatabaseMinorVersion();

			switch (majorVersion) {
			case 9:
				if (minorVersion > 2) {
					return DB.Ingres9;
				}
				return DB.Ingres;
			case 10:
				return DB.Ingres10;
			default:
				// unknown
			}
			return DB.Ingres;
		}

		if (databaseName.startsWith("Microsoft SQL Server")) {
			final int majorVersion = data.getDatabaseMajorVersion();

			switch (majorVersion) {
			case 8:
				return DB.sqlServer;
			case 9:
				return DB.sqlServer05;
			case 10:
				return DB.sqlServer08;
			case 11:
				return DB.sqlServer12;
			default:
				// unknown
			}
			return DB.sqlServer;
		}

		if ("Sybase SQL Server".equals(databaseName) || "Adaptive Server Enterprise".equals(databaseName)) {
			return DB.SybaseASE15;
		}

		if (databaseName.startsWith("Adaptive Server Anywhere")) {
			return DB.SybaseAnywhere;
		}

		if ("Informix Dynamic Server".equals(databaseName)) {
			return DB.Informix;
		}

		if ("DB2 UDB for AS/400".equals(databaseName)) {
			return DB.DB2400;
		}

		if (databaseName.startsWith("DB2/")) {
			return DB.DB2;
		}

		if ("Oracle".equals(databaseName)) {
			final int majorVersion = data.getDatabaseMajorVersion();

			switch (majorVersion) {
			case 12:
				return DB.Oracle12;
			case 11:
				// fall through
			case 10:
				return DB.Oracle10g;
			case 9:
				return DB.Oracle9i;
			case 8:
				return DB.Oracle8i;
			default:
				// unknown
			}
			return DB.Oracle8i;
		}

		if ("HDB".equals(databaseName)) {
			// SAP recommends defaulting to column store.
			return DB.HDB;
		}

		if (databaseName.startsWith("Firebird")) {
			return DB.Firebird;
		}

		return null;

	}
}
