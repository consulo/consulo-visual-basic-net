Imports System
Imports System.Collections.Generic
Imports System.Text

Namespace Colors
    Public Interface IShape
        ReadOnly Property Area As Double
    End Interface

    Public Enum Kind
        Circle
        Square
    End Enum

    Public Structure Point
        Public X As Integer
    End Structure

    Public Delegate Sub Notify(message As String)

    Public Class TagAttribute
        Inherits Attribute
    End Class

    <Tag>
    Public Class Shape(Of T)
        Implements IShape

        Public Const Sides As Integer = 4
        Public Shared Count As Integer
        Private myValue As T
        Public Event Changed As EventHandler

        Public ReadOnly Property Area As Double Implements IShape.Area
            Get
                Return Sides * 2.0
            End Get
        End Property

        Public Shared Function Create(value As T) As Shape(Of T)
            Dim shape As New Shape(Of T)()
            shape.myValue = value
            Count += 1
            Return shape
        End Function

        <Obsolete>
        Public Sub Describe(kind As Kind, point As Point)
            Dim builder As New StringBuilder()
            builder.Append(kind)
            Console.WriteLine(builder.ToString() & point.X & Math.PI)
            Dim items As New List(Of Integer)()
            For Each item In items
                Console.WriteLine(item + Integer.MaxValue)
            Next
            Describe(Kind.Square, point)
            RaiseEvent Changed(Me, EventArgs.Empty)
        End Sub
    End Class

    Public Class Union
        Public ReadOnly Property Tag As Integer

        Public NotInheritable Class Tags
            Public Const Circle As Integer = 0
        End Class

        Public Class Circle
            Inherits Union

            Public ReadOnly Property Radius As Double
        End Class
    End Class

    Module Reports
        Public Function Describe(union As Union) As String
            Select Case union.Tag
                Case Union.Tags.Circle
                    Dim c = DirectCast(union, Union.Circle)
                    Return $"circle r={c.Radius}"
            End Select
            Return ""
        End Function
    End Module

    Module Program
        Sub Main()
            Dim callback As Notify = Nothing
            Shape(Of String).Create("a").Describe(Kind.Circle, New Point())
        End Sub
    End Module
End Namespace
