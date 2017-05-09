import {Component, OnInit, Pipe, PipeTransform} from "@angular/core";
import {Game} from "../game";
import {Event} from "../event";
import {ApiRoot} from "../api-root";
import {Result, ScorecardFieldResult} from "../result";
import {ApiRootService} from "../api-root.service";
import {GameService} from "../game.service";
import {EventService} from "../event.service";
import {ResultService} from "../result.service";
import {Scorecard, FieldSection, ScorecardSection} from "../scorecard";
import {ScorecardService} from "../scorecard.service";
import {Observable, ReplaySubject} from "rxjs";
import {RobotService} from "../robot.service";
import {RobotRole} from "../robot-role";
import * as stable from "stable";

@Component({
             selector: 'app-results',
             templateUrl: './results.component.html',
             styleUrls: ['./results.component.css']
           })
export class ResultsComponent implements OnInit {
  get selectedEvent(): Event {
    return this._selectedEvent;
  }

  set selectedEvent(value: Event) {
    this._selectedEvent = value;
    this.resultsFetched = false;
    if (value) {
      this.getScorecardObservable.subscribe(
        (scorecard) => {
          let resultObservable =
            this.resultService.getResults(this.selectedEvent, scorecard)
                .do(results => console.log(JSON.stringify(results)))
                .flatMap(results => Observable.from(results))
                .do(result => console.log(JSON.stringify(result)))
                .publishReplay().refCount();
          resultObservable.subscribe();
          resultObservable.zip(resultObservable.concatMap(result => this.robotService.getRobot(result)),
                               (result, robot) => new ResultModel(Object.assign(result, {robotNumber: robot.number}))
          ).toArray().subscribe(results => {
            this.results = results;
            this.resultsFetched = true;
            console.log("results got: "+results.length);
          });
        }
      );
    }
  }

  get selectedGame(): Game {
    return this._selectedGame;
  }

  set selectedGame(value: Game) {
    this._selectedGame = value;
    if (value) {
      this.scorecardService.getScorecardsByGame(this.selectedGame)
          .flatMap(scorecards => Observable.from(scorecards))
          .first().subscribe(this.getScorecardObservable);

      this.getScorecardObservable.subscribe(scorecard => {
        this.scorecard = scorecard;
      });
      this.eventService.getEventsByGame(this.selectedGame).subscribe(
        events => {
          this.events = events;
        },
        error => console.log(error.toString())
      );
    }
  }

  games: Game[] = [];
  private _selectedGame: Game = null;
  scorecard: Scorecard;
  getScorecardObservable: ReplaySubject<Scorecard> = new ReplaySubject();

  events: Event[] = [];
  private _selectedEvent: Event;

  selectedRole: RobotRole;

  results: ResultModel[] = [];
  resultsFetched:boolean = false;

  apiRoot: ApiRoot;

  constructor(private apiRootService: ApiRootService, private gameService: GameService,
              private eventService: EventService, private resultService: ResultService,
              private scorecardService: ScorecardService, public robotService: RobotService) { }

  ngOnInit() {
    this.apiRootService.getRoot().subscribe(root => {
      this.apiRoot = root;
      console.log(JSON.stringify(root));

      this.gameService.getAllGames(root).subscribe(games => {
        this.games = games;
        console.log(JSON.stringify(games));
      });
    });
  }

  getScoreForField(result:Result,field:FieldSection):ScorecardFieldResult{
    return result.scores.filter(score=>score.field.id==field.id)[0];
  }

}

@Pipe({name: "filterSections", pure: false})
export class FilterSections implements PipeTransform {
  transform(value: any, ...args: any[]): any {
    return (<ScorecardSection[]>value).filter(section => section.sectionType == "field")
                                      .map(section => (<FieldSection>section))
                                      .sort((section1, section2) => section1.index - section2.index);
  }
}

@Pipe({name: "sortResults", pure: false})
export class SortResults implements PipeTransform {
  transform(value: any, ...args: any[]): any {
    return stable(<Result[]>value,(result1,result2)=>roleScore(result2,args[0])-roleScore(result1,args[0]));
  }

}

function roleScore(result: Result, role: RobotRole): number {
  return role.weights.map(
    weight => {
      let score = result.scores.filter(score => score.field.id == weight.field.id)[0];
      return weight.weight * (score?score.score:0);
    })
             .reduce((num, num2) => num + num2)
}

export class ResultModel extends Result {
  robotNumber: number;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
