package business;

//Clase para guardar los horarios generados
public class Schedule {
    // ej. el 00 será periodo lunes 8.00-9.00
    private int[][] periodSchedule;


    public Schedule () {

        // periodos de L-V de 8.30 a 20.30
        this.periodSchedule = new int[5][24];
    }

    public int[][] getSchedule() {
        return periodSchedule;

    }




}
