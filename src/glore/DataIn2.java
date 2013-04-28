package glore;
import java.io.Serializable;
import Jama.Matrix;

/**
 * This class is used for Client to send its computation result d and e
 * @author Wenchao
 *
 */
public class DataIn2 implements Serializable{
	private Matrix D=null;		// d <- t(x)%*%w%*%x
	private Matrix E=null;		// e <- t(x)%*%(y-p)
	private String taskName = null;
	private String type = null;	//beta or cov or auc


	public DataIn2(Matrix d, Matrix e, String taskName, String type) {
		super();
		D = d;
		E = e;
		this.taskName = taskName;
		this.type = type;
	}
	public Matrix getD() {
		return D;
	}
	public Matrix getE() {
		return E;
	}
	public String getTaskName() {
		return taskName;
	}
	public String getType() {
		return type;
	}

}
