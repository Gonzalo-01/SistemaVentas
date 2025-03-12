Sistema de Gestión de Ventas

Descripción

El "Sistema de Gestión de Ventas" es una aplicación Android desarrollada en Android Studio con Java. Permite la gestión de transacciones de ventas, el registro de usuarios, la generación de informes en PDF y la integración con Firebase Realtime Database para el almacenamiento de datos. Además, cuenta con control de acceso basado en roles.

Características

Gestor de ventas: Registro, edición y eliminación de transacciones.

Usuarios y roles: Registro de usuarios con roles de "Administrador" y "Empleado".

Generación de informes: Exportación de datos en formato PDF utilizando la biblioteca iTextPDF.

Integración con Firebase: Uso de Firebase Authentication y Firebase Realtime Database para almacenamiento seguro.

Control de acceso: Implementación de restricciones según el rol asignado al usuario.

Requisitos

Android Studio (versión recomendada 2023.1.1 o superior)

JDK 8 o superior

Dispositivo Android con versión 5.0 (Lollipop) o superior

Conexión a Internet para la sincronización con Firebase

Instalación

Clonar este repositorio:

git clone https://github.com/tu_usuario/sistema-gestion-ventas.git

Abrir el proyecto en Android Studio.

Configurar Firebase:

Crear un proyecto en Firebase.

Agregar el archivo google-services.json en la carpeta app/.

Habilitar Firebase Authentication y Firebase Realtime Database.

Compilar y ejecutar la aplicación en un emulador o dispositivo físico.

Configuración de Firebase

Accede a Firebase Console.

Crea un nuevo proyecto y agrega una aplicación Android.

Descarga y coloca el archivo google-services.json en la carpeta app/.

Habilita la autenticación por correo y contraseña.

Configura la base de datos en tiempo real con las siguientes reglas de seguridad:

{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}

Uso de la Aplicación

Registro e inicio de sesión: Los usuarios pueden registrarse e iniciar sesión con autenticación Firebase.

Gestor de ventas: Los administradores pueden agregar, editar y eliminar registros de ventas.

Generación de informes: Se pueden exportar informes de ventas en formato PDF.

Control de acceso: Los empleados tienen permisos restringidos según su rol.

Bibliotecas Utilizadas

Firebase Authentication - Manejo de autenticación de usuarios.

Firebase Realtime Database - Almacenamiento de datos en la nube.

iTextPDF - Generación de informes en PDF.

Contribución

Si deseas contribuir al proyecto:

Haz un fork del repositorio.

Crea una nueva rama (feature/nueva-funcionalidad).

Realiza tus cambios y haz un commit (git commit -m "Agrega nueva funcionalidad").

Sube tus cambios (git push origin feature/nueva-funcionalidad).

Abre un Pull Request.

Licencia

Este proyecto está bajo la licencia MIT. Puedes ver el archivo LICENSE para más detalles.
