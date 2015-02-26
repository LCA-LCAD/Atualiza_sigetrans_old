package atualiza_sigetrans;

/**
 *
 * @author Henrique
 */
public interface Type{
        
    public String getDescricao();

    public void setDescricao(String descricao);

    public Long getId();
    
    public void setId(Long id);
    
    @Override
    public int hashCode();

    @Override
    public boolean equals(Object obj);
}
