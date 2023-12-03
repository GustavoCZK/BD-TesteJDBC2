package db;

public class DbException extends RuntimeException {
	public DbException(String mensagem) {
		super("DAO: " + mensagem);
	}
}
