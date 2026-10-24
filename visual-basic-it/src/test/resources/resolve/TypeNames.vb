Imports System
Imports System.Collections
Imports Col = System.Collections

Namespace Company.Product
    Public Class Base
    End Class

    Public Class Derived(Of T)
        Inherits Base
        Implements IDisposable, ICloneable

        Private ReadOnly table As Hashtable
        Private ReadOnly list As Col.ArrayList
        Private ReadOnly qualified As System.Text.StringBuilder
        Private ReadOnly fromGlobal As Global.System.Int32
        Private ReadOnly value As T
        Private ReadOnly nested As Inner
        Private ReadOnly missing As NotExisting
        Private ReadOnly lowerCase As system.text.stringbuilder
        Private ReadOnly importedLowerCase As hashtable

        Public Function Count() As Integer
            Return 0
        End Function

        Class Inner
        End Class
    End Class
End Namespace
