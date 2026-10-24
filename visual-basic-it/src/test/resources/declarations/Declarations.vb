Imports System
Imports System.Collections.Generic
Imports System.Threading

Namespace Company.Product
    Public Class Outer(Of T)
        Inherits Base
        Implements IDisposable

        Dim count%
        Private Shared ReadOnly names() As String
        Public Const Max As Integer = 10, Min = 0
        Public WithEvents Timer As Timer
        Dim maybe? As Double, matrix(,) As Byte

        Sub New(Optional x As Integer = 1, ParamArray rest() As Object)
        End Sub

        Public Function Compute(Of U)(value As U, list As List(Of T)) As Dictionary(Of String, U)
            Return Nothing
        End Function

        Function NoType(name$)
            Return Nothing
        End Function

        Protected Friend Overridable Sub Run()
        End Sub

        Public Shared Operator +(a As Outer(Of T), b As Outer(Of T)) As Outer(Of T)
            Return a
        End Operator

        Public Shared Narrowing Operator CType(value As Outer(Of T)) As Integer
            Return 0
        End Operator

        Public Property Name As String = "n"
        ReadOnly Property Items As New List(Of Integer)
        Public Event Changed As EventHandler

        Class Inner
        End Class

        Structure Point
            Dim X As Integer
        End Structure
    End Class

    Module Helpers
        Sub Help()
        End Sub

        Dim state As Object
    End Module

    Public Interface IShape
        Function Area() As Double
    End Interface

    Enum Color As Byte
        Red
        Green = 2
    End Enum

    Delegate Function Transformer(Of T)(value As T) As T
End Namespace

Namespace Global.Other
    Class InGlobal
    End Class
End Namespace

Class TopLevel
    Declare Function Beep Lib "kernel32" (frequency As Integer, duration As Integer) As Boolean
End Class
