Imports System
Imports System.Collections.Generic
Imports System.Text

Namespace Demo
    Public Class Animal
        Public Property Name As String

        Public Overridable Function Speak() As String
            Return ""
        End Function

        Public Shared Function Create(name As String) As Animal
            Return Nothing
        End Function

        Public Shared Operator +(a As Animal, b As Animal) As Animal
            Return a
        End Operator
    End Class

    Public Class Dog
        Inherits Animal

        Public Age As Integer

        Public Function Bark(times As Integer) As String
            Return ""
        End Function

        Public Function Bark() As Integer
            Return 0
        End Function
    End Class

    Module Helpers
        Public Function Twice(x As Integer) As Long
            Return x * 2
        End Function

        Public Pi As Double = 3.14
    End Module

    Public Class Expressions
        Private ReadOnly items As New List(Of String)
        Private ReadOnly map As New Dictionary(Of String, Dog)

        Public Sub Run(arg As Integer, Optional text As String = "")
            Dim i = 1
            Dim l = 1L
            Dim d = 1.5
            Dim sum = i + l
            Dim mixed = i + d
            Dim division = i / 2
            Dim intDivision = i \ 2
            Dim power = 2 ^ 3
            Dim concat = "a" & i
            Dim plusString = "a" + "b"
            Dim compare = i < 2
            Dim logic = compare AndAlso True
            Dim bits = i And 3
            Dim negate = -i
            Dim notBool = Not compare
            Dim parameter = arg
            Dim optionalParameter = text
            Dim dog = New Dog()
            Dim name = dog.Name
            Dim age = dog.Age
            Dim speak = dog.Speak()
            Dim speakNoParentheses = dog.Speak
            Dim barkString = dog.Bark(2)
            Dim barkInteger = dog.Bark()
            Dim created = Animal.Create("x")
            Dim combined = dog + dog
            Dim first = items(0)
            Dim count = items.Count
            Dim itemLength = items(0).Length
            Dim mapped = map("a")
            Dim mappedAge = map("a").Age
            Dim chars = "abc"(1)
            Dim builder = New StringBuilder()
            Dim appended = builder.Append("x")
            Dim twice = Twice(2)
            Dim pi = Pi
            Dim casted = CType(dog, Animal)
            Dim converted = CInt("1")
            Dim isDog = TypeOf dog Is Dog
            Dim ifValue = If(compare, 1, 2)
            Dim coalesce = If(text, "default")
            Dim array = New Integer() {1, 2}
            Dim element = array(0)
            Dim matrix = New Integer(2, 3) {}
            Dim initializer = {1, 2, 3}
            Dim interpolated = $"{i}"
            Dim gotType = GetType(Dog)
            Dim self = Me
            Dim tuple = (1, "a")
            Dim maxValue = Integer.MaxValue
            Dim emptyString = String.Empty
            Dim output = Console.Out
            Dim lowerCase = DOG.AGE
            For Each item In items
                Dim fromEach = item
            Next
            For index = 0 To 10
                Dim fromFor = index
            Next
            For Each c In "text"
                Dim fromString = c
            Next
            With dog
                Dim fromWith = .Name
            End With
        End Sub
    End Class
End Namespace
