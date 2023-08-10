package br.com.alura.bytebank.domain.conta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

public class ContaDAO {

	private Connection conn;

	ContaDAO(Connection connection) {
		this.conn = connection;
	}

	public void salvar(DadosAberturaConta dadosDaConta) {
		var cliente = new Cliente(dadosDaConta.dadosCliente());
		var conta = new Conta(dadosDaConta.numero(), BigDecimal.ZERO, cliente, true);

		String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email, ativa)"
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement preparedStatement = conn.prepareStatement(sql);

			preparedStatement.setInt(1, conta.getNumero());
			preparedStatement.setBigDecimal(2, BigDecimal.ZERO);
			preparedStatement.setString(3, dadosDaConta.dadosCliente().nome());
			preparedStatement.setString(4, dadosDaConta.dadosCliente().cpf());
			preparedStatement.setString(5, dadosDaConta.dadosCliente().email());
			preparedStatement.setBoolean(6, true);

			preparedStatement.execute();
			preparedStatement.close();
			conn.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Set<Conta> listar(){
		
		PreparedStatement ps;
		ResultSet rs;
		Set<Conta> contas = new HashSet<>();
		
		String sql = "SELECT * FROM conta";
		
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while(rs.next()) {
				Integer numero = rs.getInt(1);
				BigDecimal saldo = rs.getBigDecimal(2);
				String nome = rs.getString(3);
				String cpf = rs.getString(4);
				String email = rs.getString(5);
				Boolean ativa = rs.getBoolean(6);
				
				DadosCadastroCliente dadosCliente = new DadosCadastroCliente(nome, cpf, email);
				
				Cliente cliente = new Cliente(dadosCliente);
				contas.add(new Conta(numero, saldo, cliente, ativa));
			}
			rs.close();
			ps.close();
			conn.close();
			
		} catch (SQLException e) {
			
			System.out.println("Não foi possível fzer a listagem de conta");
			System.out.println(e.getMessage());
		}
		
		return contas;
	}
	
	public Conta buscaContaPorNumero (Integer numero) {
		PreparedStatement ps;
		ResultSet rs;
		Conta conta = null;
		String sql = "SELECT * FROM conta WHERE numero = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, numero);
			rs = ps.executeQuery();
			
			while(rs.next()) {
				Integer numeroRecuperado = rs.getInt(1);
				BigDecimal saldo = rs.getBigDecimal(2);
				String nome = rs.getString(3);
				String cpf = rs.getString(4);
				String email = rs.getString(5);
				Boolean ativa = rs.getBoolean(6);
				
				DadosCadastroCliente dadosCliente = new DadosCadastroCliente(nome, cpf, email);
		
				Cliente cliente = new Cliente(dadosCliente);
				conta = new Conta(numeroRecuperado, saldo, cliente, ativa);
				
			}
			
			ps.close();
			rs.close();
			conn.close();
			
		} catch (SQLException e) {
			System.out.println("Nao foi possivel buscar essa conta: " + numero);
			
		}
		
		return conta;
		
	}
	
	public void alterar(Integer numero, BigDecimal valor) {
		PreparedStatement ps;
		String sql = "UPDATE conta SET saldo = ? WHERE numero = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setBigDecimal(1, valor);
			ps.setInt(2, numero);
			
			ps.execute();
			
			ps.close();
			conn.close();
		
		} catch (SQLException e) {
			System.out.println("Não foi possível fazer o depósito dessa conta: " + numero);
			System.out.println(e.getMessage());
			
		}
	}
	
	public void deletar(Integer numeroConta) {
		PreparedStatement ps;
		String sql = "DELETE FROM conta WHERE numero = ? and ativa = true";
		
		try {
			ps = conn.prepareStatement(sql);
			
			ps.setInt(1, numeroConta);
			
			ps.execute();
			
			ps.close();
			conn.close();
			
		} catch (SQLException e) {
			System.out.println("Não foi possível deletar essa conta: " + numeroConta);
			System.out.println(e.getMessage());
		}
	}
	
	public void alterarLogico(Integer numeroConta, Boolean ativa) {
		PreparedStatement ps;
		String sql = "UPDATE conta SET ativa = ? WHERE numero = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setBoolean(1, ativa);
			ps.setInt(2, numeroConta);
			
			ps.execute();
			
			ps.close();
			conn.close();
		
		} catch (SQLException e) {
			System.out.println("Não foi possível inativar essa conta: " + numeroConta);
			System.out.println(e.getMessage());
			
		}
	}
}
