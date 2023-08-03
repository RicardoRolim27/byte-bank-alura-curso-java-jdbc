package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

public class ContaService {

    private Set<Conta> contas = new HashSet<>();

    private ConnectionFactory connection;

    public ContaService() {
        this.connection = new ConnectionFactory();
    }

    public Set<Conta> listarContasAbertas() {
       Connection conn = connection.recuperarConexao();
       
       return new ContaDAO(conn).listar();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).salvar(dadosDaConta);
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscaContaPorNumeroNoBanco(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }
        
        if (!conta.getAtiva()) {
        	throw new RegraDeNegocioException("Conta está inativada");
        }
        
        System.out.println("realizar saque > saldo: " + conta.getSaldo());
        
        conta.sacar(valor);

        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterar(conta.getNumero(), conta.getSaldo());
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscaContaPorNumeroNoBanco(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }
        
        if (!conta.getAtiva()) {
        	throw new RegraDeNegocioException("Conta está inativada");
        }

        Connection conn = connection.recuperarConexao();
        
        BigDecimal novoValor = conta.getSaldo().add(valor);
        new ContaDAO(conn).alterar(conta.getNumero(), novoValor);
        
    }
    
    public void realizarTransferencia(Integer numeroContaOrigem, Integer numeroContaDestino, BigDecimal valor) {
    	
    	this.realizarSaque(numeroContaOrigem, valor);
    	this.realizarDeposito(numeroContaDestino, valor);
    	
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscaContaPorNumeroNoBanco(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }
        
        Connection conn = connection.recuperarConexao();
        
        new ContaDAO(conn).deletar(numeroDaConta);
    }
    
    public void encerrarLogico(Integer numeroConta) {
    	var conta = buscaContaPorNumeroNoBanco(numeroConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }
        
        Connection conn = connection.recuperarConexao();
        
    }

    private Conta buscarContaPorNumero(Integer numero) {
        return contas
                .stream()
                .filter(c -> c.getNumero() == numero)
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Não existe conta cadastrada com esse número!"));
    }
    
    public Conta buscaContaPorNumeroNoBanco(Integer numero) {
    	Connection conn = connection.recuperarConexao();
    	
    	return new ContaDAO(conn).buscaContaPorNumero(numero);
    }
}
