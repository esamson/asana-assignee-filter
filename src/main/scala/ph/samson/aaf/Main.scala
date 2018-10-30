package ph.samson.aaf

import org.scalajs.dom
import org.scalajs.dom.MutationObserver
import org.scalajs.dom.ext._
import org.scalajs.dom.raw.{HTMLDivElement, MutationObserverInit}

import scala.util.Random

object Main {

  var assignees: Set[Avatar] = Set.empty
  var onBoard = false

  /**
    * Watch if we are on a Board view
    */
  def main(args: Array[String]): Unit = {
    val main = new MutationObserver((mutations, observer) => {
      dom.document.querySelector(".BoardBody-columns") match {
        case div: HTMLDivElement =>
          if (!onBoard) {
            onBoard = true
            onBoardBodyColumns(div)
          }
        case _ =>
          onBoard = false
      }
    })
    main.observe(dom.document.body,
                 MutationObserverInit(childList = true, subtree = true))
  }

  /**
    * On board view, identify assignees and set up menu
    */
  def onBoardBodyColumns(div: HTMLDivElement): Unit = {
    assignees = Set.empty
    val boardObserver = new MutationObserver((mutations, observer) => {
      val avatars = div
        .querySelectorAll("div.DomainUserAvatar-avatar")
        .filter(_.isInstanceOf[HTMLDivElement])
        .map(_.asInstanceOf[HTMLDivElement])
        .map(Avatar.apply)
        .filter(_.isDefined)
        .map(_.get)
        .toSet
      assigneeMenu(avatars)
    })
    boardObserver.observe(
      div,
      MutationObserverInit(childList = true, subtree = true))
  }

  val menu = {
    val div = dom.document.createElement("div").asInstanceOf[HTMLDivElement]
    div.style.paddingLeft = "20px"

    div
  }

  val noAssignee = {
    val div = dom.document.createElement("div").asInstanceOf[HTMLDivElement]
    div.classList.add("DomainUserAvatar")
    div.classList.add("Avatar")
    div.classList.add("Avatar--large")
    div.classList.add(s"Avatar--color${Random.nextInt(8)}")
    div.textContent = "\u2205"

    div.onclick = event => {
      val boards = dom.document.querySelectorAll(
        "div.BoardColumnCardsContainer-draggableItemWrapper")
      for (board <- boards) {
        if (board
              .asInstanceOf[HTMLDivElement]
              .querySelector("div.DomainUserAvatar--noValue") != null) {
          board.asInstanceOf[HTMLDivElement].style.display = "inline-block"
        } else {
          board.asInstanceOf[HTMLDivElement].style.display = "none"
        }
      }
    }

    div
  }

  val allAssignees = {
    val div = dom.document.createElement("div").asInstanceOf[HTMLDivElement]
    div.classList.add("DomainUserAvatar")
    div.classList.add("Avatar")
    div.classList.add("Avatar--large")
    div.classList.add(s"Avatar--color${Random.nextInt(8)}")
    div.textContent = "\u2200"

    div.onclick = event => {
      val boards = dom.document.querySelectorAll(
        "div.BoardColumnCardsContainer-draggableItemWrapper")
      for (board <- boards) {
        board.asInstanceOf[HTMLDivElement].style.display = "inline-block"
      }
    }

    div
  }

  /**
    * Set assignee menu
    */
  def assigneeMenu(newSet: Set[Avatar]): Unit = {
    if (assignees != newSet) {
      val header = dom.document.querySelector("div.BoardHeader.Board-header")
      assignees = newSet
      if (assignees.nonEmpty) {
        while (menu.hasChildNodes()) {
          menu.removeChild(menu.lastChild)
        }
        menu.appendChild(noAssignee)
        for (assignee <- assignees) {
          menu.appendChild(assignee.div())
        }
        menu.appendChild(allAssignees)
        if (!header.childNodes.contains(menu)) {
          header.appendChild(menu)
        }
      } else {
        if (header.childNodes.contains(menu)) {
          header.removeChild(menu)
        }
      }
    }
  }

  case class Avatar(color: String,
                    style: Option[String],
                    text: Option[String]) {
    def div(): HTMLDivElement = {
      val div = dom.document.createElement("div").asInstanceOf[HTMLDivElement]
      div.classList.add("DomainUserAvatar")
      div.classList.add("Avatar")
      div.classList.add("Avatar--small")
      div.classList.add(color)
      for (s <- style) {
        div.setAttribute("style", s)
      }
      for (t <- text) {
        div.textContent = t
      }

      div.onclick = event => {
        val boards = dom.document.querySelectorAll(
          "div.BoardColumnCardsContainer-draggableItemWrapper")
        for (board <- boards) {
          val assignee = board
            .asInstanceOf[HTMLDivElement]
            .querySelector("div.DomainUserAvatar-avatar")

          if (assignee != null) {
            for (s <- style) {
              if (assignee.hasAttribute("style") && assignee.getAttribute(
                    "style") == s) {
                board.asInstanceOf[HTMLDivElement].style.display =
                  "inline-block"
              } else {
                board.asInstanceOf[HTMLDivElement].style.display = "none"
              }
            }
            for (t <- text) {
              if (assignee.textContent == t) {
                board.asInstanceOf[HTMLDivElement].style.display =
                  "inline-block"
              } else {
                board.asInstanceOf[HTMLDivElement].style.display = "none"
              }
            }
          } else {
            board.asInstanceOf[HTMLDivElement].style.display = "none"
          }
        }
      }

      div
    }
  }

  object Avatar {
    def apply(div: HTMLDivElement): Option[Avatar] = {
      val classes = div.classList
      var color: Option[String] = None
      for (i <- 0 to classes.length) {
        val className = classes.item(i)
        if (className != null && className.contains("color")) {
          color = Some(className)
        }
      }
      color.map(c =>
        if (div.hasAttribute("style")) {
          Avatar(c, Some(div.getAttribute("style")), None)
        } else {
          Avatar(c, None, Some(div.textContent))
      })
    }
  }
}
