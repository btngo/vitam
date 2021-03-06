import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Event} from '../event';

@Component({
  selector: 'vitam-event-display',
  templateUrl: './event-display.component.html',
  styleUrls: ['./event-display.component.css']
})
export class EventDisplayComponent implements OnInit {
  @Input() event: Event;
  @Input() childLevel = 0;
  public logbookRowStyle = '';
  public isParent = false;
  public hideChildren = true;
  public showEvDetData = false;
  private stepStatusIcon: string;
  private indentChildClass: string;
  private stepMessageClass: string;

  constructor() { }

  ngOnInit() {
    if (this.event.end) {
      if (this.event.end.evType.toUpperCase().indexOf('.STARTED') > -1) {
        this.stepStatusIcon = 'fa-circle-o-notch fa-spin';
        this.logbookRowStyle = 'okRow';
      } else {
        this.setStepStatusIcon(this.event.end.outcome);
      }
    } else {
      this.stepStatusIcon = 'fa-circle-o-notch fa-spin';
      this.logbookRowStyle = 'okRow';
    }

    if (this.event.subEvents) {
      if (this.event.subEvents.length > 0) {
        this.isParent = true;
      }
    } else {
      if (!this.event.end) {
        this.event.end = this.event.start;
      }
    }

    if (this.event.end.outcome === 'KO') {
      this.hideChildren = false;
    }

    this.indentChildClass = `ui-g-${this.childLevel}`;
    this.stepMessageClass = `ui-g-${11 - this.childLevel}`;
  }

  public getCaretType() {
    if (this.isParent) {
      if (this.hideChildren) {
        return 'fa fa-caret-down fa-2x';
      } else {
        return 'fa fa-caret-up fa-2x';
      }
    }
  }

  public toggleChildren() {
    this.hideChildren = !this.hideChildren;
  }

  private setStepStatusIcon (stepStatus: string) {
    switch (stepStatus.toUpperCase()) {
      case 'KO':
      case 'FATAL':
        this.stepStatusIcon = 'fa-times';
        this.logbookRowStyle = 'errorRow';
        break;
      case 'OK':
        this.stepStatusIcon = 'fa-check';
        this.logbookRowStyle = 'okRow';
        break;
      case 'WARNING':
        this.stepStatusIcon = 'fa-exclamation';
        this.logbookRowStyle = 'warningRow';
        break;
      default:
        this.stepStatusIcon = '';
    }
  }
}
