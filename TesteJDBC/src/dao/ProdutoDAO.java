package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.FabricaConexao;
import db.DbException;
import modelo.Produto;

public class ProdutoDAO implements IProdutoDAO {
	private Connection conexao;
	
	@Override
	public List<Produto> ListarTodos() {
		conexao = FabricaConexao.getConexao();
		List<Produto> lista = new ArrayList<>();
		
		try {
			String sql = "SELECT * FROM produto";
			PreparedStatement stmt = this.conexao.prepareStatement(sql);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Produto ct = setaProduto(rs);
				
				lista.add(ct);
			} 
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			FabricaConexao.fecharConexao();
		}
		
		return lista;
			
		}


	private Produto setaProduto(ResultSet rs) throws SQLException {
		Produto ct = new Produto();
		
		ct.setIdProd(rs.getInt("id_prod"));
		ct.setNomeProd(rs.getString("nome_prod"));
		ct.setDataCadastro(rs.getDate("data_cadastro"));
		ct.setQuantidade(rs.getInt("quantidade"));
		ct.setPreco(rs.getBigDecimal("preco"));
		return ct;
	}
	
	@Override
	public Map<String, Integer> salvar(Produto p) {
		conexao = FabricaConexao.getConexao();
		
		Map<String, Integer> retorno = new HashMap<>() {{
			put("afetados", 0);
			put("chave", null);
		}};
		
		String sql;
		if (p.getIdProd() == null) {
			sql = "INSERT INTO produto (nome_prod, data_cadastro, quantidade, preco) "
					+ "VALUES (?,?,?,?);";
		} else {
			sql = "UPDATE produto SET nome_prod=?, data_cadastro=?, quantidade=?, preco=? "
					+ "WHERE id_prod=?";
		}
		
		try (
			PreparedStatement stmt = this.conexao.prepareStatement(sql,
													Statement.RETURN_GENERATED_KEYS);
		) {
			
			stmt.setString(1, p.getNomeProd());
			stmt.setDate(2, p.getDataCadastro());
			stmt.setInt(3, p.getQuantidade());
			stmt.setBigDecimal(4, p.getPreco());
			
			if (p.getIdProd() != null) {
				stmt.setInt(5, p.getIdProd());
			}

			//Qunatos registros foram afetados
			conexao.setAutoCommit(false); 					// Desativa o commit automático
			int registrosAfetados = stmt.executeUpdate();	// Executa o SQL
			retorno.put("afetados", registrosAfetados);		// Quantos registros foram afetados
			
			// No Caso de inserção, Retorna ID gerado
			try (ResultSet rs = stmt.getGeneratedKeys()){
				if(rs.next()) {
					retorno.put("chave", rs.getInt(1));
				}
			}
			conexao.commit(); 			//comita os dados no banco
			return retorno;
			
		} catch (SQLException e) {
			try {
				conexao.rollback();
				throw new DbException("Transação revertida (rolled back)! " + e.getMessage());
			} catch (SQLException e1) {
				throw new DbException("ihhh, deu ruim! " + e1.getMessage());
			}
		} finally {
			FabricaConexao.fecharConexao();
		}
	}
	
	public boolean excluir(Integer id) {
		System.out.println("Excluindo o registro " + id);
		conexao = FabricaConexao.getConexao();
		String sql = "DELETE FROM produto WHERE id_prod=?";
		
		try (
			PreparedStatement stmt = this.conexao.prepareStatement(sql);
		) {
			stmt.setInt(1, id);
			conexao.setAutoCommit(false);
			int registrosAfetados = stmt.executeUpdate();
					
			if (1 == 1) {
				throw new SQLException();
			}
			
			conexao.commit();
			
			return (registrosAfetados == 1);
		} catch (SQLException e) {
			try {
				conexao.rollback();
				throw new DbException("Transação revertida (rolled back)!\n"
										+ "Registros " + id + "não foi excluido!!\n"
										+ e.getMessage());
			} catch (SQLException e1) {
				throw new DbException("ihhh, deu ruim" + e1.getMessage());
			}
		} finally {
			FabricaConexao.fecharConexao();
		}
	}
	
	public Produto buscarPorId(Integer id) {
		conexao = FabricaConexao.getConexao();
		String sql = "SELECT * FROM produto WHERE id_prod=?";
		Produto p = null;

		try (
				PreparedStatement stmt = this.conexao.prepareStatement(sql);
		){
			stmt.setInt(1, id);

			try (ResultSet rs = stmt.executeQuery()) {
				if(rs.next()) {
				p = setaProduto(rs);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			FabricaConexao.fecharConexao();
		}
		return p;
	}
	
	@Override
	public int getNumeroRegistros() {
		conexao = FabricaConexao.getConexao();
		String sql = "SELECT count(*) FROM produto";
		int numRegistros = 0;
		
		try ( 
			PreparedStatement stmt = this.conexao.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
		) {
			if (rs.next()) {
				numRegistros = rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			FabricaConexao.fecharConexao();
		}
			return numRegistros;
		}
	
	public List<Produto> buscarPorNome(String nome) {
		conexao = FabricaConexao.getConexao();
		List<Produto> lista = new ArrayList<>();
		String sql = "SELECT * FROM produto WHERE nome_prod LIKE ?";
		
		try (
			PreparedStatement stmt = this.conexao.prepareStatement(sql);
		) { 
			stmt.setString(1, "%" + nome + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Produto p = setaProduto(rs);
					lista.add(p);
				}
			}
			
		} catch (SQLException e){
			throw new RuntimeException(e.getMessage());
		} finally {
			FabricaConexao.fecharConexao();
		}
		return lista;
	}
}

