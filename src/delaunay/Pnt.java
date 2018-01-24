package delaunay;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pnt {
    private double[] coordinates;          
    public Pnt (double... coords) {
        coordinates = new double[coords.length];
        System.arraycopy(coords, 0, coordinates, 0, coords.length);
    }

    @Override
    public String toString () {
        if (coordinates.length == 0) return "Pnt()";
        String result = "Pnt(" + coordinates[0];
        for (int i = 1; i < coordinates.length; i++)
            result = result + "," + coordinates[i];
        result = result + ")";
        return result;
    }
//    @Override
//    public boolean equals (Object other) {
//        if (!(other instanceof Pnt)) return false;
//        Pnt p = (Pnt) other;
//        if (this.coordinates.length != p.coordinates.length) return false;
//        for (int i = 0; i < this.coordinates.length; i++)
//            if (this.coordinates[i] != p.coordinates[i]) return false;
//        return true;
//    }

//    @Override
//    public int hashCode () {
//        int hash = 0;
//        for (double c: this.coordinates) {
//            long bits = Double.doubleToLongBits(c);
//            hash = (31*hash) ^ (int)(bits ^ (bits >> 32));
//        }
//        return hash;
//    }


    public double coord (int i) {
        return this.coordinates[i];
    }

   
    public int dimension () {
        return coordinates.length;
    }

   
    public int dimCheck (Pnt p) {
        int len = this.coordinates.length;
        return len;
    }

    
    public Pnt extend (double... coords) {
        double[] result = new double[coordinates.length + coords.length];
        System.arraycopy(coordinates, 0, result, 0, coordinates.length);
        System.arraycopy(coords, 0, result, coordinates.length, coords.length);
        return new Pnt(result);
    }

    public double dot (Pnt p) {
        int len = dimCheck(p);
        double sum = 0;
        for (int i = 0; i < len; i++)
            sum += this.coordinates[i] * p.coordinates[i];
        return sum;
    }

    
    public Pnt subtract (Pnt p) {
        int len = dimCheck(p);
        double[] coords = new double[len];
        for (int i = 0; i < len; i++)
            coords[i] = this.coordinates[i] - p.coordinates[i];
        return new Pnt(coords);
    }

   
    public Pnt add (Pnt p) {
        int len = dimCheck(p);
        double[] coords = new double[len];
        for (int i = 0; i < len; i++)
            coords[i] = this.coordinates[i] + p.coordinates[i];
        return new Pnt(coords);
    }

   
    public Pnt bisector (Pnt point) {
        dimCheck(point);
        Pnt diff = this.subtract(point);
        Pnt sum = this.add(point);
        double dot = diff.dot(sum);
        return diff.extend(-dot / 2);
    }

   
    public static double determinant (Pnt[] matrix) {
        
        boolean[] columns = new boolean[matrix.length];
        for (int i = 0; i < matrix.length; i++) columns[i] = true;
        try {return determinant(matrix, 0, columns);}
        catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("");
        }
    }

  
    private static double determinant(Pnt[] matrix, int row, boolean[] columns){
        if (row == matrix.length) return 1;
        double sum = 0;
        int sign = 1;
        for (int col = 0; col < columns.length; col++) {
            if (!columns[col]) continue;
            columns[col] = false;
            sum += sign * matrix[row].coordinates[col] *
                   determinant(matrix, row+1, columns);
            columns[col] = true;
            sign = -sign;
        }
        return sum;
    }

  
    public static Pnt cross (Pnt[] matrix) {
        int len = matrix.length + 1;
      
        boolean[] columns = new boolean[len];
        for (int i = 0; i < len; i++) columns[i] = true;
        double[] result = new double[len];
        int sign = 1;
        try {
            for (int i = 0; i < len; i++) {
                columns[i] = false;
                result[i] = sign * determinant(matrix, 0, columns);
                columns[i] = true;
                sign = -sign;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("");
        }
        return new Pnt(result);
    }

  
    public static double content (Pnt[] simplex) {
        Pnt[] matrix = new Pnt[simplex.length];
        for (int i = 0; i < matrix.length; i++)
            matrix[i] = simplex[i].extend(1);
        int fact = 1;
        for (int i = 1; i < matrix.length; i++) fact = fact*i;
        return determinant(matrix) / fact;
    }

    public int[] relation (Pnt[] simplex) {
      
        int dim = simplex.length - 1;
        Pnt[] matrix = new Pnt[dim+1];
        double[] coords = new double[dim+2];
        for (int j = 0; j < coords.length; j++) coords[j] = 1;
        matrix[0] = new Pnt(coords);
        for (int i = 0; i < dim; i++) {
            coords[0] = this.coordinates[i];
            for (int j = 0; j < simplex.length; j++)
                coords[j+1] = simplex[j].coordinates[i];
            matrix[i+1] = new Pnt(coords);
        }

        Pnt vector = cross(matrix);
        double content = vector.coordinates[0];
        int[] result = new int[dim+1];
        for (int i = 0; i < result.length; i++) {
            double value = vector.coordinates[i+1];
            if (Math.abs(value) <= 1.0e-6 * Math.abs(content)) result[i] = 0;
            else if (value < 0) result[i] = -1;
            else result[i] = 1;
        }
        if (content < 0) {
            for (int i = 0; i < result.length; i++)
                result[i] = -result[i];
        }
        if (content == 0) {
            for (int i = 0; i < result.length; i++)
                result[i] = Math.abs(result[i]);
        }
        return result;
    }

   
    public Pnt isOutside (Pnt[] simplex) {
        int[] result = this.relation(simplex);
        for (int i = 0; i < result.length; i++) {
            if (result[i] > 0) return simplex[i];
        }
        return null;
    }

   
    public int vsCircumcircle (Pnt[] simplex) {
        Pnt[] matrix = new Pnt[simplex.length + 1];
        for (int i = 0; i < simplex.length; i++)
            matrix[i] = simplex[i].extend(1, simplex[i].dot(simplex[i]));
        matrix[simplex.length] = this.extend(1, this.dot(this));
        double d = determinant(matrix);
        int result = (d < 0)? -1 : ((d > 0)? +1 : 0);
        if (content(simplex) < 0) result = - result;
        return result;
    }

   
    public static Pnt circumcenter (Pnt[] simplex) {
        int dim = simplex[0].dimension();
    
        Pnt[] matrix = new Pnt[dim];
        for (int i = 0; i < dim; i++)
            matrix[i] = simplex[i].bisector(simplex[i+1]);
        Pnt hCenter = cross(matrix);     
        double last = hCenter.coordinates[dim];
        double[] result = new double[dim];
        for (int i = 0; i < dim; i++) result[i] = hCenter.coordinates[i] / last;
        return new Pnt(result);
    }
    //计算面积的函数
    public static Double area(Pnt[] simplex)
    {
    	String areaString1=simplex[0].toString();
    	String areaString2=simplex[1].toString();
    	String areaString3=simplex[2].toString();
     	String []area1=matcher(areaString1);
    	String[] area2=matcher(areaString2);
    	String[] area3=matcher(areaString3);
		double [] a=new double[6];
		for (int i = 0; i < area1.length; i++) {
			String string = area1[i];
   			a[i]=Double.valueOf(string);
		}
		for (int i = 0; i < area2.length; i++) {
			String string=area2[i];
			a[i+2]=Double.valueOf(string);
		}
		for (int i = 0; i < area3.length; i++) {
			String string=area3[i];
			a[i+4]=Double.valueOf(string);
		}
		System.out.println("三角形的面积");
		double area=Math.abs((a[0]*a[3]+a[1]*a[4]+a[2]*a[5]-a[3]*a[4]-a[1]*a[2]-a[0]*a[5])/2);
    	return area;
    }
    //计算重心的函数
    public static Pnt  Focus(Pnt[] simplex) {
    	 Pnt resultfocus=simplex[0].add(simplex[1].add(simplex[2]));//计算重心的第一步    	 
    	 String result=resultfocus.toString();
    	 String[] strings = matcher(result);
    	 System.out.println("三角形的重心是：");
    	 double b=0,c=0;
    	  for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
   			Double a=Double.valueOf(string);
   			a=a/3;
   			if (i==0) {
				b=a;
			}
   			if (i==1) {
				c=a;
			}
   			
   			string=String.valueOf(a);
   			strings[i]=string;
   			System.out.println(strings[i]);
          }
    	  Pnt resultfocusPnt=new Pnt(b,c);
		return resultfocusPnt;
	}
    private static Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");

	public static String[] matcher(String input) {
        Matcher matcher = pattern.matcher(input);
		List<String> list = new ArrayList();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list.toArray(new String[0]);
    }
}