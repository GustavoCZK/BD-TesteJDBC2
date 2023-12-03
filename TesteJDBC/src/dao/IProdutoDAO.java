package dao;

import java.util.List;
import java.util.Map;

import modelo.Produto;

public interface IProdutoDAO {
	Map<String, Integer> salvar(Produto p);
	boolean excluir(Integer id);
	List<Produto> ListarTodos();
	Produto buscarPorId(Integer id);
	List<Produto> buscarPorNome(String nome);
	int getNumeroRegistros();
}
