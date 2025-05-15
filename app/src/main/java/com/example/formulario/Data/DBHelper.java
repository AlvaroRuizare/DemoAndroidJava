package com.example.formulario.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "DBFormulario";
    private static final int DATABASE_VERSION = 2;


    // *** TABLA USUARIOS ***
    private static final String USUARIOS_TABLE_NAME = "Usuarios";
    private static final String USUARIOS_COLUMN_ID = "id";
    private static final String USUARIOS_COLUMN_USUARIO = "usuario";
    private static final String USUARIOS_COLUMN_CONTRASENA = "contrasena";
    private static final String USUARIOS_COLUMN_CORREO = "correo";
    private static final String USUARIOS_COLUMN_FECHANACIMIENTO = "fechaNacimiento";
    private static final String USUARIOS_COLUMN_TIPOUSUARIO = "tipoUsuario";
    private static final String USUARIOS_COLUMN_DEBAJA = "deBaja";


    // *** TABLA TIPOS USUARIO ***
    private static final String TIPOSUSUARIO_TABLE_NAME = "TiposUsuario";
    private static final String TIPOSUSUARIO_COLUMN_ID = "id";
    private static final String TIPOSUSUARIO_COLUMN_TIPOUSUARIO = "tipoUsuario";


    // *** TABLA MULTIMEDIA ***
    private static final String MULTIMEDIA_TABLE_NAME = "Multimedia";
    private static final String MULTIMEDIA_COLUMN_ID = "id";
    private static final String MULTIMEDIA_COLUMN_IDUSUARIO = "idUsuario";
    private static final String MULTIMEDIA_COLUMN_RUTA = "ruta";


    // *** TABLA LOCALIZACION ***
    private static final String LOCALIZACION_TABLE_NAME = "Localizacion";
    private static final String LOCALIZACION_COLUMN_ID = "id";
    private static final String LOCALIZACION_COLUMN_IDUSUARIO = "idUsuario";
    private static final String LOCALIZACION_COLUMN_COORDENADASX = "coordenadasX";
    private static final String LOCALIZACION_COLUMN_COORDENADASY = "coordenadasY";


    /**
     * Constructor de la clase DBHelper
     */
    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

        // context.deleteDatabase(DATABASE_NAME);

        this.getReadableDatabase(); // Se llama a onCreate
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String usuariosCrearTabla = "CREATE TABLE " + USUARIOS_TABLE_NAME + " (" +
                USUARIOS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USUARIOS_COLUMN_USUARIO + " TEXT, " +
                USUARIOS_COLUMN_CONTRASENA + " TEXT, " +
                USUARIOS_COLUMN_CORREO + " TEXT, " +
                USUARIOS_COLUMN_FECHANACIMIENTO + " TEXT, " +
                USUARIOS_COLUMN_TIPOUSUARIO + " TEXT," +
                USUARIOS_COLUMN_DEBAJA + " INTEGER DEFAULT 0" + // Se crea columna sin OnUpgrade (razón dentro del OnUpgrade)
                ");";

        String tiposUsuarioCrearTabla = "CREATE TABLE " + TIPOSUSUARIO_TABLE_NAME + " (" +
                TIPOSUSUARIO_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TIPOSUSUARIO_COLUMN_TIPOUSUARIO + " TEXT, " +
                "FOREIGN KEY (" + TIPOSUSUARIO_COLUMN_ID + ") REFERENCES " + USUARIOS_TABLE_NAME + "(" + USUARIOS_COLUMN_TIPOUSUARIO + ")" +
                ");";

        String multimediaCrearTabla = "CREATE TABLE " + MULTIMEDIA_TABLE_NAME + " (" +
                MULTIMEDIA_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MULTIMEDIA_COLUMN_IDUSUARIO + " INT, " +
                MULTIMEDIA_COLUMN_RUTA + " TEXT, " +
                "FOREIGN KEY (" + MULTIMEDIA_COLUMN_IDUSUARIO + ") REFERENCES " + USUARIOS_TABLE_NAME + "(" + USUARIOS_COLUMN_ID + ")" +
                ");";

        String localizacionCrearTabla = "CREATE TABLE " + LOCALIZACION_TABLE_NAME + " (" +
                LOCALIZACION_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOCALIZACION_COLUMN_IDUSUARIO + " INT, " +
                LOCALIZACION_COLUMN_COORDENADASX + " REAL, " +
                LOCALIZACION_COLUMN_COORDENADASY + " REAL, " +
                "FOREIGN KEY (" + LOCALIZACION_COLUMN_IDUSUARIO + ") REFERENCES " + USUARIOS_TABLE_NAME + "(" + USUARIOS_COLUMN_ID + ")" +
                ");";

        db.execSQL(usuariosCrearTabla);
        db.execSQL(tiposUsuarioCrearTabla);
        db.execSQL(multimediaCrearTabla);
        db.execSQL(localizacionCrearTabla);
        inicializarTiposUsuario(db); // Se rellena la tabla 'TiposUsuario'
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + USUARIOS_TABLE_NAME + ";");
        //db.execSQL("DROP TABLE IF EXISTS " + TIPOSUSUARIO_TABLE_NAME + ";");
        //onCreate(db);

        // ONUPGRADE VERSION 2 (AÑADIR COLUMNA DE BAJA)
        //if (oldVersion < 2) {
        //    db.execSQL("ALTER TABLE " + USUARIOS_TABLE_NAME +
        //                " ADD COLUMN " + USUARIOS_COLUMN_DEBAJA + " INTEGER DEFAULT 0");
        //}

        // PARA LA VERSION 3 (TABLA MULTIMEDIA) BORRAMOS LA BD Y LA CREAMOS DE NUEVO
        // PORQUE TENEMOS QUE BORRAR UNA COLUMNA DE USUARIOS Y LOS ONUPGRADE PIERDEN SENTIDO AL
        // REFERENCIAR ESA COLUMNA EN EL RESTO DEL CÓDIGO. Esto también causa que el
        // OnUpgrade de la versión 2 pierda sentido.
    }


    /**
     * Comprueba si un dato existe en la base de datos
     *
     * @param tabla   la tabla en la que buscar
     * @param columna la columna en la que buscar
     * @param cadena  la cadena que buscar
     */
    public boolean existeEnBD(String tabla, String columna, String cadena) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean existeEnBD = false;

        // Conseguir usuario y contraseña
        String consultaUsuario = "SELECT " + columna + " FROM " + tabla + " WHERE " + columna + " = '" + cadena + "'";
        Cursor cursorUsuario = db.rawQuery(consultaUsuario, null); // Se asignan los datos del select al cursor

        if (cursorUsuario.getCount() != 0) // Si existe el dato
            existeEnBD = true; // Se devuelve true

        return existeEnBD;
    }


    /**
     * La primera vez que se inicia la aplicación, inicializa los tipos de usuario en la BD
     */
    public void inicializarTiposUsuario(SQLiteDatabase db) {
        ContentValues cv = new ContentValues(); // Se crea el contenido que añadir

        cv.put(TIPOSUSUARIO_COLUMN_TIPOUSUARIO, "Usuario"); // Se añade al contenido
        db.insert(TIPOSUSUARIO_TABLE_NAME, null, cv); // Se inserta el contenido en la bd

        cv.put(TIPOSUSUARIO_COLUMN_TIPOUSUARIO, "Administrador"); // Se añade al contenido
        db.insert(TIPOSUSUARIO_TABLE_NAME, null, cv); // Se inserta el contenido en la bd
    }


    /**
     * Devuelve cursor con los tipos de usuario que hay en la BD
     */
    public Cursor obtenerTiposUsuario() {
        String consulta = "SELECT " + TIPOSUSUARIO_COLUMN_TIPOUSUARIO + " FROM " + TIPOSUSUARIO_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        return cursor;
    }


    /**
     * Devuelve el ID del usuario
     */
    public int obtenerIdUsuario(String usuarioRecibido){
        String consulta = "SELECT " + USUARIOS_COLUMN_ID +
                " FROM " + USUARIOS_TABLE_NAME +
                " WHERE " + USUARIOS_COLUMN_USUARIO + " = '" + usuarioRecibido + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        cursor.moveToFirst();
        return cursor.getInt(0);
    }


    /**
     * Devuelve el tipo de usuario que es el usuario recibido
     */
    public int obtenerTipoUsuario(String nombreUsuario) {
        String consulta = "SELECT " + USUARIOS_COLUMN_TIPOUSUARIO +
                " FROM " + USUARIOS_TABLE_NAME +
                " WHERE " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_USUARIO + " = '" + nombreUsuario + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        cursor.moveToFirst();
        return cursor.getInt(0);
    }


    /**
     * Devuelve el correo del usuario recibido
     */
    public String obtenerCorreoUsuario(String nombreUsuario) {
        String consulta = "SELECT " + USUARIOS_COLUMN_CORREO + " FROM " + USUARIOS_TABLE_NAME + " WHERE " + USUARIOS_COLUMN_USUARIO + " = '" + nombreUsuario + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        cursor.moveToFirst();

        return cursor.getString(0);
    }


    /**
     * Devuelve las coordenadas X del usuario recibido
     */
    public Double obtenerCoordenadasX(int idUsuario) {
        String consulta = "SELECT " + LOCALIZACION_COLUMN_COORDENADASX + " FROM " + LOCALIZACION_TABLE_NAME + " WHERE " + LOCALIZACION_COLUMN_IDUSUARIO + " = " + idUsuario;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        cursor.moveToFirst();

        return cursor.getDouble(0);
    }


    /**
     * Devuelve las coordenadas X del usuario recibido
     */
    public Double obtenerCoordenadasY(int idUsuario) {
        String consulta = "SELECT " + LOCALIZACION_COLUMN_COORDENADASY + " FROM " + LOCALIZACION_TABLE_NAME + " WHERE " + LOCALIZACION_COLUMN_IDUSUARIO + " = " + idUsuario;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        cursor.moveToFirst();

        return cursor.getDouble(0);
    }


    /**
     * Devuelve todas las fotos del usuario recibido
     */
    public ArrayList<String> obtenerRutaImagenes(String nombreUsuarioEditar, String nombreUsuarioLogueado) {
        ArrayList<String> rutasImagen = new ArrayList<>();

        String consulta;

        if (obtenerTipoUsuario(nombreUsuarioLogueado) == 2){ // Si el usuario logueado es admin...
            // Mostrar fotos del usuario que estamos editando y luego las de el resto de usuarios
            consulta = "SELECT " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_RUTA +
                    " FROM " + MULTIMEDIA_TABLE_NAME +
                    " INNER JOIN " + USUARIOS_TABLE_NAME +
                    " ON " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_IDUSUARIO + " = " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID +
                    " WHERE " + USUARIOS_COLUMN_USUARIO + " = '" + nombreUsuarioEditar + "'" +

                    " UNION ALL " +

                    "SELECT " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_RUTA +
                    " FROM " + MULTIMEDIA_TABLE_NAME +
                    " INNER JOIN " + USUARIOS_TABLE_NAME +
                    " ON " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_IDUSUARIO + " = " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID +
                    " WHERE " + USUARIOS_COLUMN_USUARIO + " != '" + nombreUsuarioEditar + "'";
        } else { // Si no es admin...
            // Mostrar solo fotos del usuario
            consulta = "SELECT " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_RUTA +
                    " FROM " + MULTIMEDIA_TABLE_NAME +
                    " INNER JOIN " + USUARIOS_TABLE_NAME +
                    " ON " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_IDUSUARIO + " = " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID +
                    " WHERE " + USUARIOS_COLUMN_USUARIO + " = '" + nombreUsuarioEditar + "'";
        }

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        cursor.moveToFirst();
        // Por cada dato...
        do{
            // Se añade a la lista la primera columna que devuelve la consulta
            rutasImagen.add(cursor.getString(0));
        } while (cursor.moveToNext());

        return rutasImagen;
    }


    /**
     * Devuelve si el usuario está de baja
     */
    public boolean estaDebaja(String usuarioRecibido){
        SQLiteDatabase db = this.getReadableDatabase();
        boolean deBaja = false;

        // Conseguir usuario y contraseña de la BD
        String consultaUsuario = "SELECT " + USUARIOS_COLUMN_DEBAJA +
                " FROM " + USUARIOS_TABLE_NAME +
                " WHERE " + USUARIOS_COLUMN_USUARIO + " = '" + usuarioRecibido + "'";

        Cursor cursorUsuario = db.rawQuery(consultaUsuario, null); // Se asignan los datos del select al cursor
        cursorUsuario.moveToFirst();

        if (cursorUsuario.getCount() != 0) { // Si hay datos...
            // Comprobar si se devuelven datos
            int intDeBaja = cursorUsuario.getInt(0); // Se guarda baja en formato int
            if(intDeBaja == 1){ // Se obtiene boolean a partir del resultado int
                deBaja = true;
            }
        }

        return deBaja;
    }


    /**
     * Devuelve cursor con los usuarios que hay en la BD
     */
    public Cursor obtenerUsuarios(String usuarioLogueado) {
        String consulta;

        if (obtenerTipoUsuario(usuarioLogueado) == 2) { // Si el usuario logueado es administrador...
            // Obtiene todos los usuarios
            consulta = "SELECT " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_USUARIO + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_CONTRASENA + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_CORREO + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_FECHANACIMIENTO + ", " +
                    TIPOSUSUARIO_TABLE_NAME + "." + TIPOSUSUARIO_COLUMN_TIPOUSUARIO + ", " +
                    MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_RUTA + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_DEBAJA +
                    " FROM " + USUARIOS_TABLE_NAME +
                    " INNER JOIN " + TIPOSUSUARIO_TABLE_NAME +
                    " ON " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_TIPOUSUARIO + " = " + TIPOSUSUARIO_TABLE_NAME + "." + TIPOSUSUARIO_COLUMN_ID +
                    " INNER JOIN " + MULTIMEDIA_TABLE_NAME +
                    " ON " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID + " = " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_IDUSUARIO +
                    " GROUP BY " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID;
        } else { // Si el usuario logueado es de otro tipo...
            // Obtiene solo usuarios normales
            consulta = "SELECT " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_USUARIO + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_CONTRASENA + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_CORREO + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_FECHANACIMIENTO + ", " +
                    TIPOSUSUARIO_TABLE_NAME + "." + TIPOSUSUARIO_COLUMN_TIPOUSUARIO + ", " +
                    MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_RUTA + ", " +
                    USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_DEBAJA +
                    " FROM " + USUARIOS_TABLE_NAME +
                    " INNER JOIN " + TIPOSUSUARIO_TABLE_NAME +
                    " ON " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_TIPOUSUARIO + " = " + TIPOSUSUARIO_TABLE_NAME + "." + TIPOSUSUARIO_COLUMN_ID +
                    " INNER JOIN " + MULTIMEDIA_TABLE_NAME +
                    " ON " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID + " = " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_IDUSUARIO +
                    " WHERE " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_TIPOUSUARIO + " = 1" + // 1 = Tipo Usuario
                    " GROUP BY " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        return cursor;
    }


    /**
     * Comprueba si las credenciales del login son correctas
     */
    public boolean credencialesCorrectas(String usuarioRecibido, String contrasenaRecibida) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean credencialesCorrectas = false;

        // Conseguir usuario y contraseña de la BD
        String consultaUsuario = "SELECT " + USUARIOS_COLUMN_USUARIO + ", " + USUARIOS_COLUMN_CONTRASENA +
                " FROM " + USUARIOS_TABLE_NAME +
                " WHERE " + USUARIOS_COLUMN_USUARIO + " = '" + usuarioRecibido + "'";
        Cursor cursorUsuario = db.rawQuery(consultaUsuario, null); // Se asignan los datos del select al cursor

        if (cursorUsuario.getCount() != 0) { // Si existen datos...
            cursorUsuario.moveToFirst(); // Se mueve el cursor a la primera posición

            String usuariobd = cursorUsuario.getString(0); // Se guarda usuario BD
            String contrasenabd = cursorUsuario.getString(1); // Se guarda contraseña BD

            // Si coinciden usuario y contraseña...
            if ((Objects.equals(usuariobd, usuarioRecibido)) && // Coincide usuario
                    (Objects.equals(contrasenabd, contrasenaRecibida))) { // Coincide contraseña
                credencialesCorrectas = true; // Se devuelve true
            }
        }

        return credencialesCorrectas;
    }


    /**
     * Inserta un usuario en la base de datos
     */
    public void insertarUsuario(Double coordenadasX, Double coordenadasY, String rutaFoto, String usuario, String contrasena, String correo, String fechaNacimiento, String tipoUsuario, Snackbar snackbar) {
        SQLiteDatabase db = this.getWritableDatabase(); // Se consigue la BD

        // Se crea el contenido que añadir
        ContentValues cvUsuarios = new ContentValues();
        ContentValues cvMultimedia = new ContentValues();
        ContentValues cvLocalizacion= new ContentValues();

        if (existeEnBD(USUARIOS_TABLE_NAME, USUARIOS_COLUMN_USUARIO, usuario)) { // Si ya existe usuario...
            snackbar.setText("El usuario ya existe en la base de datos").show();
        } else if (existeEnBD(USUARIOS_TABLE_NAME, USUARIOS_COLUMN_CORREO, correo)) { // Si ya existe correo...
            snackbar.setText("El correo ya existe en la base de datos").show();
        } else {
            try {
                // Insertar datos usuario
                cvUsuarios.put(USUARIOS_COLUMN_USUARIO, usuario);
                cvUsuarios.put(USUARIOS_COLUMN_CONTRASENA, contrasena);
                cvUsuarios.put(USUARIOS_COLUMN_CORREO, correo);
                cvUsuarios.put(USUARIOS_COLUMN_FECHANACIMIENTO, fechaNacimiento);
                // Controlar tipo usuario introducido
                if (tipoUsuario.equals("Usuario")) {
                    cvUsuarios.put(USUARIOS_COLUMN_TIPOUSUARIO, 1);
                } else if (tipoUsuario.equals("Administrador")) {
                    cvUsuarios.put(USUARIOS_COLUMN_TIPOUSUARIO, 2);
                }
                long resultadoUsuarios = db.insert(USUARIOS_TABLE_NAME, null, cvUsuarios);

                // Insertar datos multimedia
                cvMultimedia.put(MULTIMEDIA_COLUMN_IDUSUARIO, obtenerIdUsuario(usuario));
                cvMultimedia.put(MULTIMEDIA_COLUMN_RUTA, rutaFoto);
                long resultadoMultimedia = db.insert(MULTIMEDIA_TABLE_NAME, null, cvMultimedia);

                // Insertar datos localizacion
                cvLocalizacion.put(LOCALIZACION_COLUMN_IDUSUARIO, obtenerIdUsuario(usuario));
                cvLocalizacion.put(LOCALIZACION_COLUMN_COORDENADASX, coordenadasX);
                cvLocalizacion.put(LOCALIZACION_COLUMN_COORDENADASY, coordenadasY);
                long resultadoLocalizacion = db.insert(LOCALIZACION_TABLE_NAME, null, cvLocalizacion);

                // Si el resultado es -1...
                if (resultadoUsuarios == -1) {
                    snackbar.setText("Error creando los datos de usuario").show();
                } else if (resultadoMultimedia == -1) {
                    snackbar.setText("Error creando los datos de multimedia").show();
                } else if (resultadoLocalizacion == -1) {
                    snackbar.setText("Error creando los datos de localización").show();
                } else {
                    snackbar.setText("Se ha creado la cuenta correctamente").show();
                }
            } catch (Exception e){
                snackbar.setText("Ha ocurrido un error al insertar los datos");
                snackbar.show();
            }
        }
    }


    /**
     * Actualiza los datos de un usuario
     */
    public int actualizarUsuario(String nombreNuevo, String contrasena, String correoNuevo, String fechaNacimiento, String tipoUsuario, int deBaja, String nombreUsuarioAEditar, Snackbar snackbar) {
        SQLiteDatabase db = this.getWritableDatabase();
        int codigoSalida = 0;

        // Si las credenciales son válidas
        if (valoresNoRepetidos(nombreUsuarioAEditar, nombreNuevo, obtenerCorreoUsuario(nombreUsuarioAEditar), correoNuevo, snackbar)) {
            ContentValues cvUsuarios = new ContentValues();
            // Se añade al contenido
            cvUsuarios.put(USUARIOS_COLUMN_USUARIO, nombreNuevo);
            cvUsuarios.put(USUARIOS_COLUMN_CONTRASENA, contrasena);
            cvUsuarios.put(USUARIOS_COLUMN_CORREO, correoNuevo);
            cvUsuarios.put(USUARIOS_COLUMN_FECHANACIMIENTO, fechaNacimiento);
            // Controlar tipo usuario introducido
            if (tipoUsuario.equals("Usuario")) {
                cvUsuarios.put(USUARIOS_COLUMN_TIPOUSUARIO, 1);
            } else if (tipoUsuario.equals("Administrador")) {
                cvUsuarios.put(USUARIOS_COLUMN_TIPOUSUARIO, 2);
            }
            cvUsuarios.put(USUARIOS_COLUMN_DEBAJA, deBaja);
            long resultadoUsuarios = db.update(USUARIOS_TABLE_NAME, cvUsuarios, "usuario=?", new String[]{nombreUsuarioAEditar});

            /*ContentValues cvMultimedia = new ContentValues();
            cvMultimedia.put(MULTIMEDIA_COLUMN_IDUSUARIO, idUsuario(nombreUsuarioAEditar));
            cvMultimedia.put(MULTIMEDIA_COLUMN_RUTA, rutaFoto);
            long resultadoMultimedia = db.update(MULTIMEDIA_TABLE_NAME, cvMultimedia, "idUsuario=?", new String[]{String.valueOf(idUsuario(nombreUsuarioAEditar))});*/

            // Si el resultado es -1...
            if (resultadoUsuarios == -1) {
                snackbar.setText("Error creando los datos de usuario").show();
                codigoSalida = 1;
            }
            /*else if (resultadoMultimedia == -1) {
                snackbar.setText("Error creando los datos de multimedia").show();
            } */
            else {
                snackbar.setText("Se han guardado los datos").show();
            }
        }

        return codigoSalida;
    }


    /**
     * Borra un usuario
     */
    public void borrarUsuario(String nombreUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Borramos usuario filtrando por nombreUsuario en la columna usuario
        db.delete(USUARIOS_TABLE_NAME, "usuario=?", new String[]{nombreUsuario});
        db.close();
    }


    /**
     * Comprueba si hay diferencia entre los valores
     */
    public boolean haCambiadoValor(String valorOriginal, String valorNuevo) {
        boolean haCambiadoValor = false;

        if (!valorOriginal.equals(valorNuevo)) { // Si ha cambiado el valor...
            haCambiadoValor = true; // Devuelve true
        }

        return haCambiadoValor;
    }


    /**
     * Comprueba si los usuarios existen ya en BD
     */
    public boolean valoresNoRepetidos(String nombreUsuarioAntiguo, String nombreUsuarioNuevo, String correoAntiguo, String correoNuevo, Snackbar snackbar) {
        boolean todoCorrecto = true;

        if (haCambiadoValor(nombreUsuarioAntiguo, nombreUsuarioNuevo)) {
            if (existeEnBD(USUARIOS_TABLE_NAME, USUARIOS_COLUMN_USUARIO, nombreUsuarioNuevo)) { // Se comprueba si el usuario existe en BD...
                todoCorrecto = false;
                snackbar.setText("El usuario ya existe en la base de datos").show();
            }
        }

        if (haCambiadoValor(correoAntiguo, correoNuevo)) { // Si se ha cambiado el correo...
            if (existeEnBD(USUARIOS_TABLE_NAME, USUARIOS_COLUMN_CORREO, correoNuevo)) { // Se comprueba si el correo existe en BD...
                todoCorrecto = false;
                snackbar.setText("El correo ya existe en la base de datos").show();
            }
        }

        return todoCorrecto;
    }


    /**
     * Guarda la ruta de una foto nueva en BD
     */
    public void guardarRutaFotoNueva(String usuarioRecibido, String rutaFoto){
        SQLiteDatabase db = this.getWritableDatabase(); // Se consigue la BD
        ContentValues cv = new ContentValues();

        // Se añade al contenido
        cv.put(MULTIMEDIA_COLUMN_IDUSUARIO, obtenerIdUsuario(usuarioRecibido));
        cv.put(MULTIMEDIA_COLUMN_RUTA, rutaFoto);
        db.insert(MULTIMEDIA_TABLE_NAME, null, cv);
    }


    /**
     * Obtiene el idMultimedia de la primera foto de ese usuario
     */
    public int obtenerIdPrimeraFotoUsuario(String usuarioRecibido){
        int idPrimeraFoto = 0;

        String consulta = "SELECT " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_ID +
                " FROM " + MULTIMEDIA_TABLE_NAME +
                " INNER JOIN " + USUARIOS_TABLE_NAME +
                " ON " + MULTIMEDIA_TABLE_NAME + "." + MULTIMEDIA_COLUMN_IDUSUARIO + " = " + USUARIOS_TABLE_NAME + "." + USUARIOS_COLUMN_ID +
                " WHERE " + USUARIOS_COLUMN_USUARIO + " = '" + usuarioRecibido + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) // Si la base de datos tiene datos...
            cursor = db.rawQuery(consulta, null); // Se asignan los datos del select al cursor

        cursor.moveToFirst();
        idPrimeraFoto = cursor.getInt(0); // Se guarda el primer idMultimedia que devuelve la consulta

        return idPrimeraFoto;
    }


    /**
     * Modifica la ruta de la primera foto del usuario (la foto de perfil)
     */
    public void cambiarRutaFotoPerfil(String nombreUsuario, String nuevaRutaFoto){
        SQLiteDatabase db = this.getWritableDatabase(); // Se consigue la BD
        ContentValues cv = new ContentValues();

        cv.put(MULTIMEDIA_COLUMN_RUTA, nuevaRutaFoto);
        db.update(MULTIMEDIA_TABLE_NAME, cv, "id=?", new String[]{String.valueOf(obtenerIdPrimeraFotoUsuario(nombreUsuario))});
    }
}
